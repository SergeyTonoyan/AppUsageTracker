package net.abraksis.appusagetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ReceiverBootCompleted extends BroadcastReceiver {

    public ReceiverBootCompleted() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isStartForeground = preferences.getBoolean("pref_show_icon", false);
        Intent startIntent = new Intent(context, MainService.class);
        if (isStartForeground) {

            intent.putExtra("isStartForeground", true);
        }
        context.startService(startIntent);

        boolean isSendDailyNotifications = preferences.getBoolean("pref_send_daily_notif", true);

        DailyReminder reminder = new DailyReminder();
        if (isSendDailyNotifications){
            reminder.setupAlarm(context, MainActivity.NOTIFICATION_TIME_HOUR,
                    MainActivity.NOTIFICATION_TIME_MINUTE);
        } else {
            reminder.cancelAlarm(context);
        }
    }
}
