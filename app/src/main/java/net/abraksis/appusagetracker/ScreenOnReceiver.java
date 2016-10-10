package net.abraksis.appusagetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class ScreenOnReceiver extends BroadcastReceiver {
    public ScreenOnReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            UsageStatsDataSource dataSource = new UsageStatsDataSource(context);
            Calendar date = Calendar.getInstance();
            dataSource.increaseUnlocksCount(date);
        }
    }
}
