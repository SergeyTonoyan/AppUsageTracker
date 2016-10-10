package net.abraksis.appusagetracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBHelper  extends SQLiteOpenHelper {

    public final static String DATABASE_NAME = "myDB";
    public final static String APP_USAGE_STATS_TABLE_NAME = "app_usage_stats";
    public final static String PHONE_UNLOCKS_STATS_TABLE_NAME = "phone_unlocks_stats";
    public final static String DATE_COLUMN = "date";
    public final static String APP_NAME_COLUMN = "app_name";
    public final static String WORKING_TIME_COLUMN = "working_time";
    public final static String UNLOCKS_COUNT_COLUMN = "unlocks_count";

    private final static String APP_USAGE_TABLE_CREATE_QUERY = "create table if not exists "
                + APP_USAGE_STATS_TABLE_NAME + " (" + BaseColumns._ID
                + " integer primary key autoincrement, " + APP_NAME_COLUMN
                + " text not null, " + WORKING_TIME_COLUMN + " integer, "
                + DATE_COLUMN + " integer);";

    private final static String PHONE_UNLOCKS_STATS_CREATE_QUERY = "create table if not exists "
            + PHONE_UNLOCKS_STATS_TABLE_NAME + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + UNLOCKS_COUNT_COLUMN
            + " integer, " + DATE_COLUMN + " integer);";

    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(APP_USAGE_TABLE_CREATE_QUERY);
        db.execSQL(PHONE_UNLOCKS_STATS_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
