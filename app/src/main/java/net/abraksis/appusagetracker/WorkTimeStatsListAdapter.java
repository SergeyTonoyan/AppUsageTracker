package net.abraksis.appusagetracker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import static net.abraksis.appusagetracker.R.string.hour;
import static net.abraksis.appusagetracker.R.string.minute;

public class WorkTimeStatsListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Application> apps;
    private LayoutInflater layoutInflater;

    public WorkTimeStatsListAdapter(Context context, ArrayList<Application> applications) {

        this.context = context;
        this.apps = applications;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {

        return apps.size();
    }

    @Override
    public Object getItem(int position) {

        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.work_time_stats_list_item, parent, false);
        }

        Application app = apps.get(position);
        setAppNameText(view, app);
        setWorkingTimeText(view,app);
        setAppIcon(view, app);
        setProgressBar(view, app);
        return view;
    }

    private void setAppNameText(View view, Application app) {
        
        TextView tvName = (TextView) view.findViewById(R.id.tvName);
        String packageName = app.getPackageName();
        String appName = AppNameConverter.getAppNameFromPackageName(context, packageName);
        tvName.setText(appName);
    }

    private void setWorkingTimeText(View view, Application app) {

        TextView tvWorkTime = (TextView) view.findViewById(R.id.tvWorkTime);
        int workingTimeMilliSec = app.getWorkTimeMilliSec();
        int hours = TimeHelper.getHour(workingTimeMilliSec);
        int minutes = TimeHelper.getMinutes(workingTimeMilliSec);
        int seconds = TimeHelper.getSeconds(workingTimeMilliSec);
        tvWorkTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void setAppIcon(View view, Application app) {
        Drawable icon = null;
        try {
            icon = context.getPackageManager().getApplicationIcon(app.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ((ImageView) view.findViewById(R.id.ivIcon)).setImageDrawable(icon);
    }

    private void setProgressBar(View view, Application app) {
        ProgressBar pbWorkingTime = (ProgressBar) view.findViewById(R.id.pbWorkingTime);
        long maxWorkingTime = apps.get(0).getWorkTimeMilliSec();
        int workingTime = app.getWorkTimeMilliSec();
        pbWorkingTime.setProgress(0);
        pbWorkingTime.setMax((int)  maxWorkingTime);
        pbWorkingTime.setProgress(workingTime);
    }
}
