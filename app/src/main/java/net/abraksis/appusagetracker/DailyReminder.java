package net.abraksis.appusagetracker;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import java.util.Calendar;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class DailyReminder extends BroadcastReceiver{

    private static final int DAY_MILLISECONDS = 24 * 60 * 60 * 1000;
    private static final int NOTIFICATION_ID = 555;
    private static final int BLINK_INTERVAL = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {

        Calendar firstDate = Calendar.getInstance();
        firstDate.set(Calendar.HOUR_OF_DAY, 0);
        firstDate.add(Calendar.DAY_OF_MONTH, -1); //previous day
        Calendar secondDate = Calendar.getInstance();
        secondDate.set(Calendar.HOUR_OF_DAY, 0);
        UsageStatsDataSource dataSource = new UsageStatsDataSource(context);
        Application mostUsedApp = dataSource.getMostUsedApp(firstDate, secondDate);
        int totalUsageTime = dataSource.getTotalUsageTimeByDate(firstDate, secondDate);
        String title = getNotificationTitle(context, totalUsageTime);
        String text = getNotificationText(context, mostUsedApp);
        Notification notification = buildNotification(context, title, text);
        notify(context, notification);
    }

    private String getNotificationTitle(Context context, int totalUsageTime) {

        int totalUsageTimeHour = TimeHelper.getHour(totalUsageTime);
        int totalUsageTimeMinute = TimeHelper.getMinutes(totalUsageTime);
        String title = context.getString(R.string.every_day_notification_title) +
                " " + totalUsageTimeHour +
                context.getString(R.string.hour) + " " +
                totalUsageTimeMinute +
                context.getString(R.string.minute);
        return title;
    }

    private String getNotificationText(Context context, Application application) {

        int appWorkingTime = application.getWorkTimeMilliSec();
        int appWorkingTimeHour = TimeHelper.getHour(appWorkingTime);
        int appWorkingTimeMinute = TimeHelper.getMinutes(appWorkingTime);
        String appPackageName = application.getPackageName();
        String appName = AppNameConverter.getAppNameFromPackageName(context,appPackageName);
        String text = context.getString(R.string.every_day_notification_text) +
                appName + " (" +
                appWorkingTimeHour +
                context.getString(R.string.hour) +
                " " + appWorkingTimeMinute +
                context.getString(R.string.minute) + ")";
        return text;
    }

    private Notification buildNotification(Context context, String title, String text) {

        Intent notificationIntent = new Intent();
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.notification_icon);
        builder.setAutoCancel(true);
        builder.setLights(Color.BLUE, BLINK_INTERVAL, BLINK_INTERVAL);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setContentIntent(pendingIntent);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(uri);
        builder.build();
        Notification notification = builder.getNotification();
        return notification;
    }

    private void notify(Context context, Notification notification) {

        NotificationManager notificationManager =
                (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public  void setupAlarm(Context context, int hour, int minute) {

        Intent intent = new Intent(context, DailyReminder.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_MONTH, 1);
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), DAY_MILLISECONDS,
                pendingIntent);
    }

    public  void cancelAlarm(Context context) {

        Intent intent = new Intent(context, DailyReminder.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
