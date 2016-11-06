package net.abraksis.appusagetracker;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String BUNDLE_APPS = "apps";
    public static final String BUNDLE_TOTAL_USAGE_TIME = "total_usage_time";
    public static final String BUNDLE_UNLOCKS_COUNT = "unlocks_count";
    public static final String BUNDLE_IS_START_FOREGROUND = "isStartForeground";
    public static final String TAG = "appUsageTrackerLogs";

    public static final int NOTIFICATION_TIME_HOUR = 8;
    public static final int NOTIFICATION_TIME_MINUTE = 30;

    private UsageStatsFragment usageStatsFragment;
    private DialogFragment allowPermissionDialogFragment;
    private UsageStatsDataSource dataSource;

    private SharedPreferences preferences;
    private Spinner intervalSpinner;
    private boolean isStartForeground;
    private Interval interval;
    private boolean isSendDailyNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        dataSource = new UsageStatsDataSource(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initSettings();
        readSettings();
        restartService();
        setupDailyNotifications();
    }

    private void initViews() {

        initIntervalSpinner();
        initToolbar();
        initFragments();
    }

    private void initSettings() {

        PreferenceManager.setDefaultValues(this, R.xml.fragment_settings, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void readSettings() {

        isStartForeground = preferences.getBoolean(getString(R.string.pref_show_icon), false);
        isSendDailyNotifications = preferences.getBoolean(getString(R.string.pref_send_daily_notif),
                true);
    }

    private void restartService() {

        Intent intent = new Intent(this, MainService.class);
        if (isStartForeground) {

            intent.putExtra(BUNDLE_IS_START_FOREGROUND, true);
        }
        this.stopService(intent);
        this.startService(intent);
    }

    private void setupDailyNotifications() {

        DailyReminder reminder = new DailyReminder();
        if (isSendDailyNotifications){
            reminder.setupAlarm(this, NOTIFICATION_TIME_HOUR, NOTIFICATION_TIME_MINUTE);
        } else {
            reminder.cancelAlarm(this);
        }
    }

    private void initIntervalSpinner() {
        intervalSpinner = (Spinner) findViewById(R.id.interval_spinner);
        ArrayAdapter<?> adapter =
                ArrayAdapter.createFromResource(this, R.array.interval_values, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        intervalSpinner.setAdapter(adapter);
        intervalSpinner.setOnItemSelectedListener(this);
    }

    private void initToolbar() {
        Toolbar actionBarToolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.toolbar_icon);
        actionBarToolbar.setNavigationIcon(drawable);
        setSupportActionBar(actionBarToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initFragments() {
        allowPermissionDialogFragment = new AllowPermissionDialogFragment();
        usageStatsFragment = new UsageStatsFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_about:
                return true;
            case R.id.action_refresh:
                restartService(); //force to restart service
                updateFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {

        super.onResume();
        readSettings();
        if (Build.VERSION.SDK_INT >= 21) {
            showPermissionDialog();
        }
        updateFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showPermissionDialog() {

        AppOpsManager appOps = (AppOpsManager) this
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(),
                this.getPackageName());
        boolean isGranted = mode == AppOpsManager.MODE_ALLOWED;

        if (isGranted == false) {

            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            allowPermissionDialogFragment.show(fragmentManager,
                    getString(R.string.allow_permission_dialog_fragment_tag));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        
        readSettings();
        if (key.equals(getString(R.string.pref_show_icon))) {
            restartService();
        }
        if (key.equals(getString(R.string.pref_send_daily_notif))) {
            setupDailyNotifications();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        interval = Interval.DAY;
        switch (i) {
            case 0: interval = Interval.DAY;
                break;
            case 1: interval = Interval.WEEK;
                break;
            case 2: interval = Interval.MONTH;
                break;
        }
        restartService();//force to restart service
        updateFragment();
    }

    private void updateFragment() {

        ArrayList<Application> appsList = dataSource.getAppsUsageStatsByInterval(interval);
        int totalUsageTime = dataSource.getTotalUsageTimeByInterval(interval);
        int unlocksCount = dataSource.getUnlockCountByInterval(interval);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        usageStatsFragment = new UsageStatsFragment();
        Bundle data = new Bundle();
        data.putParcelableArrayList(BUNDLE_APPS, appsList);
        data.putInt(BUNDLE_TOTAL_USAGE_TIME, totalUsageTime);
        data.putInt(BUNDLE_UNLOCKS_COUNT, unlocksCount);
        usageStatsFragment.setArguments(data);

        fragmentManager.beginTransaction()
                .replace(R.id.fragContainer, usageStatsFragment)
                .commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
