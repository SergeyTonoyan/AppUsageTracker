package net.abraksis.appusagetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

public class UsageStatsDataSource {

    private DBHelper dbHelper;

    public UsageStatsDataSource(Context context) {

        this.dbHelper = new DBHelper(context);
    }

    public void writeDataToAppUsageStatsTable(String appName, long workingTime) {

        Calendar date = Calendar.getInstance();
        String dateStr = convertDateToString(date);

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        String columns[] = {dbHelper.WORKING_TIME_COLUMN};
        String selection = "app_name = ? and date = ?";
        String selectionArgs[] = {appName, dateStr};
        Cursor cursor = database.query(dbHelper.APP_USAGE_STATS_TABLE_NAME, columns, selection,
                selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.moveToFirst();

        if (count > 0) {

            database = dbHelper.getWritableDatabase();
            int workingTimeColumnIndex = cursor.getColumnIndex(dbHelper.WORKING_TIME_COLUMN);
            long currentWorkingTime = cursor.getInt(workingTimeColumnIndex);
            long newWorkingTime = currentWorkingTime + workingTime;
            contentValues.put(dbHelper.WORKING_TIME_COLUMN, newWorkingTime);
            database.update(dbHelper.APP_USAGE_STATS_TABLE_NAME, contentValues, selection, selectionArgs);

        } else {

            database = dbHelper.getWritableDatabase();
            contentValues.put(dbHelper.APP_NAME_COLUMN, appName);
            contentValues.put(dbHelper.WORKING_TIME_COLUMN, workingTime);
            contentValues.put(dbHelper.DATE_COLUMN, Integer.valueOf(dateStr));
            database.insert(dbHelper.APP_USAGE_STATS_TABLE_NAME, null, contentValues);
        }

        database.close();
        cursor.close();
    }

    public void increaseUnlocksCount(Calendar date) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        String columns[] = {dbHelper.DATE_COLUMN, dbHelper.UNLOCKS_COUNT_COLUMN};
        String selection = "date = ?";
        String dateStr = convertDateToString(date);
        String selectionArgs[] = {dateStr};
        Cursor cursor = database.query(DBHelper.PHONE_UNLOCKS_STATS_TABLE_NAME, columns, selection,
                selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.moveToFirst();

        if (count > 0) {

            database = dbHelper.getWritableDatabase();
            int unlocksCountColumnIndex = cursor.getColumnIndex(DBHelper.UNLOCKS_COUNT_COLUMN);
            int unlocksCount = cursor.getInt(unlocksCountColumnIndex);
            unlocksCount++;
            contentValues.put(DBHelper.UNLOCKS_COUNT_COLUMN, unlocksCount);
            database.update(DBHelper.PHONE_UNLOCKS_STATS_TABLE_NAME, contentValues, selection, selectionArgs);

        } else {

            database = dbHelper.getWritableDatabase();
            contentValues.clear();
            contentValues.put(DBHelper.DATE_COLUMN, dateStr);
            contentValues.put(DBHelper.UNLOCKS_COUNT_COLUMN, 1);
            database.insert(DBHelper.PHONE_UNLOCKS_STATS_TABLE_NAME, null, contentValues);
        }
        cursor.close();
        database.close();
    }

    public ArrayList<Application> getAppsUsageStatsByInterval(Interval interval) {


        Calendar secondDate = Calendar.getInstance();
        Calendar firstDate = IntervalManager.getIntervalFirstDate(interval, secondDate);
        ArrayList<Application> appsList = getAppsUsageStatsByDate(firstDate, secondDate);
        return appsList;
    }

    public ArrayList<Application> getAppsUsageStatsByDate(Calendar firstDate, Calendar secondDate) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        ArrayList<Application> appsList = new ArrayList<Application>();
        String columns[] = new String[] {dbHelper.APP_NAME_COLUMN, "SUM(" +
                dbHelper.WORKING_TIME_COLUMN + ")" + "" +
                " AS " + dbHelper.WORKING_TIME_COLUMN};
        String firstDateString = convertDateToString(firstDate);
        String secondDateString = convertDateToString(secondDate);
        String selection = "date BETWEEN ? AND ?";
        String selectionArgs[] = {firstDateString, secondDateString};

        Cursor cursor = database.query(dbHelper.APP_USAGE_STATS_TABLE_NAME, columns, selection,
                selectionArgs, dbHelper.APP_NAME_COLUMN, null,
                dbHelper.WORKING_TIME_COLUMN + " DESC");

        cursor.moveToFirst();

        int workingTimeColumnIndex = cursor.getColumnIndex(dbHelper.WORKING_TIME_COLUMN);
        int appNameColumnIndex = cursor.getColumnIndex(dbHelper.APP_NAME_COLUMN);

        int count = cursor.getCount();
        Application app = null;

        for (int i = 0; i < count; i++) {

            String appName = cursor.getString(appNameColumnIndex);
            int workingTime = cursor.getInt(workingTimeColumnIndex);
            app = new Application(appName, workingTime);
            appsList.add(app);
            cursor.moveToNext();
        }

        cursor.close();
        database.close();
        return appsList;
    }

    public int getTotalUsageTimeByInterval(Interval interval) {

        Calendar secondDate = Calendar.getInstance();
        Calendar firstDate = IntervalManager.getIntervalFirstDate(interval, secondDate);
        int totalWorkingTime = getTotalUsageTimeByDate(firstDate, secondDate);
        return totalWorkingTime;
    }

    public int getTotalUsageTimeByDate(Calendar firstDate, Calendar secondDate) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        ContentValues contentValues = new ContentValues();

        String columns[] = new String[] {"SUM(" + dbHelper.WORKING_TIME_COLUMN + ")" + "" +
                " AS " + dbHelper.WORKING_TIME_COLUMN};
        String firstDateString = convertDateToString(firstDate);
        String secondDateString = convertDateToString(secondDate);
        String selection = "date BETWEEN ? AND ?";
        String selectionArgs[] = {firstDateString, secondDateString};

        Cursor cursor = database.query(dbHelper.APP_USAGE_STATS_TABLE_NAME, columns, selection, selectionArgs,
                null, null, null);
        cursor.moveToFirst();
        int workingTimeColumnIndex = cursor.getColumnIndex(dbHelper.WORKING_TIME_COLUMN);
        int workingTime = cursor.getInt(workingTimeColumnIndex);
        cursor.close();
        database.close();
        return workingTime;
    }

    public int getUnlockCountByInterval(Interval interval) {

        Calendar secondDate = Calendar.getInstance();
        Calendar firstDate = IntervalManager.getIntervalFirstDate(interval, secondDate);
        int unlocksCount = getUnlocksCountByDate(firstDate, secondDate);
        return unlocksCount;
    }

    public Application getMostUsedApp(Calendar firstDate, Calendar secondDate) {

        ArrayList<Application> apps = getAppsUsageStatsByDate(firstDate, secondDate);
        Application mostUsedApp = null;

        if (apps.isEmpty()) {
            mostUsedApp = new Application("unknown", 0);
        } else {
            mostUsedApp = apps.get(0);
        }
        return mostUsedApp;
    }

    public int getUnlocksCountByDate(Calendar firstDate, Calendar secondDate) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        ContentValues contentValues = new ContentValues();

        String columns[] = new String[] {"SUM(" + dbHelper.UNLOCKS_COUNT_COLUMN + ")" + "" +
                " AS " + dbHelper.UNLOCKS_COUNT_COLUMN};
        String firstDateString = convertDateToString(firstDate);
        String secondDateString = convertDateToString(secondDate);
        String selection = "date BETWEEN ? AND ?";
        String selectionArgs[] = {firstDateString, secondDateString};

        Cursor cursor = database.query(dbHelper.PHONE_UNLOCKS_STATS_TABLE_NAME, columns, selection, selectionArgs,
                null, null, null);
        cursor.moveToFirst();
        int unlocksCountColumnIndex = cursor.getColumnIndex(dbHelper.UNLOCKS_COUNT_COLUMN);
        int unlocksCount = cursor.getInt(unlocksCountColumnIndex);
        cursor.close();
        database.close();
        return unlocksCount;
    }

    private String convertDateToString(Calendar calendar){

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) +1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int date = year * 10000 + month * 100 + day;
        String result = Integer.toString(date);
        return result;
    }
}
