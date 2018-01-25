package net.abraksis.appusagetracker;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.lang.Thread.sleep;

public class MainService extends Service {

    private static final int NOTIFICATION_ID = 10;
    private final int WRITE_TO_DB_INTERVAL_TICKS = 60;
    private final int UPDATE_NOTIFICATION_INTERVAL_TICKS = 120;
    private final int TICK_INTERVAL_MILLISECS = 500;
    private final int APP_NAME_NOT_FOUND = -1;
    private boolean isMainThreadRun;
    private boolean isStartForeground = true;

    private  ArrayList<Application> apps;

    private long beginIterationTime;
    private long endIterationTime;
    private long iterationTime;

    private ActivityManager activityManager;
    private UsageStatsDataSource dataSource;
    private ScreenOnReceiver screenOnReceiver;

    public MainService() {

        apps = new ArrayList<Application>();
        isMainThreadRun = true;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        initScreenOnRecevier();
        iterationTime = 0;
        dataSource = new UsageStatsDataSource(this);
        Log.d(MainActivity.TAG, "service created ");
    }

    private void initScreenOnRecevier() {

        screenOnReceiver = new ScreenOnReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenOnReceiver, filter);
    }

    @Override
    public void onDestroy() {

        isMainThreadRun = false;
        unregisterReceiver(screenOnReceiver);
        writeDataToDB();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(MainActivity.TAG, "service started ");

        if (intent != null) {

            isStartForeground = intent.getBooleanExtra(MainActivity.BUNDLE_IS_START_FOREGROUND,
                    false);
        }

        if (isStartForeground) {

            createOngoingNotification();
        }

        Thread myThread = new Thread(new Runnable()
        {

            public void run()
            {

               int tick = 0;

               while (isMainThreadRun) {

                   beginIterationTime = SystemClock.elapsedRealtime();
                   tick++;
                   pause(TICK_INTERVAL_MILLISECS);
                   String activeAppFullPackageName = getActiveAppFullPackageName();

                   if (activeAppFullPackageName != null) {

                       updateRunningAppStats(activeAppFullPackageName, iterationTime);
                   }

                   showCurrentStatsInLogs();
                   endIterationTime = SystemClock.elapsedRealtime ();
                   iterationTime = endIterationTime - beginIterationTime;

                   if ((tick % WRITE_TO_DB_INTERVAL_TICKS) == 0) {

                       writeDataToDB();
                   }

                   if ((tick % UPDATE_NOTIFICATION_INTERVAL_TICKS) == 0) {

                       updateNotification();
                   }
               }
            }
        });

        myThread.start();
        return START_STICKY;
    }

    private void createOngoingNotification() {

        Notification notification = buildNotification("Title", "Text");
        startForeground(NOTIFICATION_ID, notification);
        updateNotification();
    }

    private void showCurrentStatsInLogs() {
        for (int i = 0; i < apps.size(); i++) {

            Log.d(MainActivity.TAG, apps.get(i).getPackageName() + "  " + apps.get(i).getWorkTimeMilliSec());
        }
    }

    private Notification buildNotification(String title, String text) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(false);
        builder.setSmallIcon(R.drawable.notification_icon);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setPriority(Notification.PRIORITY_MIN);
        builder.setShowWhen(false);
        builder.build();
        Notification notification = builder.getNotification();
        return notification;
    }

    private void updateNotification() {

        int totalUsageTime = dataSource.getTotalUsageTimeByInterval(Interval.DAY);
        int unlocksCount = dataSource.getUnlockCountByInterval(Interval.DAY);
        int hour = TimeHelper.getHour(totalUsageTime);
        int minutes = TimeHelper.getMinutes(totalUsageTime);
        String title = getString(R.string.notification_title) + " " + hour + getString(R.string.hour)
                + " " + minutes + getString(R.string.minute);
        String text = getString(R.string.notification_text) + " " + unlocksCount;
        Notification notification = buildNotification(title, text);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void pause(int pauseTimeMilliSec) {

        try {
            sleep(pauseTimeMilliSec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getActiveAppFullPackageName() {

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        String activeAppFullPackageName = null;
        if (isScreenOn) {

            if (Build.VERSION.SDK_INT >= 21) {

                activeAppFullPackageName = getActiveAppNameNewAndroid();

            } else {
                activeAppFullPackageName = getActiveAppNameOldAndroid();
            }

        }
        return activeAppFullPackageName;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private String getActiveAppNameNewAndroid() {

        UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService("usagestats");
        long currentTime = System.currentTimeMillis();
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
               currentTime - 3600 * 1000 * 4, currentTime);
        String result = null;
        if(usageStatsList != null) {
            SortedMap<Long,UsageStats> mySortedMap = new TreeMap<Long,UsageStats>();
            for (UsageStats usageStats : usageStatsList) {
                mySortedMap.put(usageStats.getLastTimeUsed(),usageStats);
            }
            if(mySortedMap != null && !mySortedMap.isEmpty()) {
                result =  mySortedMap.get(mySortedMap.lastKey()).getPackageName();
            }
        }
        return result;
    }

    private String getActiveAppNameOldAndroid() {

        return activityManager.getRunningTasks(1)
                .get(0)
                .topActivity
                .getPackageName();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void updateRunningAppStats(String appFullPackageName, long time) {

        String appPackageName = getPackageNameFromProcessName(appFullPackageName);
        int appIndex = getAppIndexByName(appPackageName);
        if (appIndex != APP_NAME_NOT_FOUND) {

            apps.get(appIndex).increaseWorkingTime(time);
        } else {

            Application app = new Application(appPackageName, 0);
            apps.add(app);
        }
    }

    public void writeDataToDB() {

        int appsSize = apps.size();
        Application app;

        for (int i = 0; i < appsSize; i++) {

            app = apps.get(i);
            long  workTime = app.getWorkTimeMilliSec();
            String appName = app.getPackageName();
            dataSource.writeDataToAppUsageStatsTable(appName, workTime);
        }
        apps.clear();
        Log.d(MainActivity.TAG, "data has been written");
    }

    private String getPackageNameFromProcessName(String processName) {

        String result ="";
        for (int i = 0; i < processName.length() ; i++) {

            if (processName.charAt(i) == ':') {
                break;
            }
            result += processName.charAt(i);
        }
        return result;
    }

    private int getAppIndexByName(String appName) {

        // return -1 if appName wasn't found
        int result = APP_NAME_NOT_FOUND;

        for (int i = 0; i < apps.size(); i++) {

            Application app = apps.get(i);

            if (app.getPackageName().equals(appName)) {

                result = i;
                break;
            }
        }
        return result;
    }
}
