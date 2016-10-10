package net.abraksis.appusagetracker;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class UsageStatsFragment extends Fragment {

    private WorkTimeStatsListAdapter listAdapter;
    private int totalUsageTime;
    private int unlocksCount;
    private ListView listView;
    private TextView appUsageStatsCaptionTextView;
    private TextView appUsageStatsTextView;
    private TextView unlocksCountTextView;
    ArrayList<Application> appsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        appsList = bundle.getParcelableArrayList(MainActivity.BUNDLE_APPS);
        totalUsageTime = bundle.getInt(MainActivity.BUNDLE_TOTAL_USAGE_TIME);
        unlocksCount = bundle.getInt(MainActivity.BUNDLE_UNLOCKS_COUNT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.usage_stats_fragment,
                container, false);
        initViews(view);
        updateFragment();
        return view;
    }

    private void initViews(View view) {

        listView = (ListView) view.findViewById(R.id.usageStatsListView);
        appUsageStatsCaptionTextView = (TextView) view.findViewById(R.id.totalUsageTimeCaptionTextView);
        appUsageStatsTextView = (TextView) view.findViewById(R.id.totalUsageTimeTextView);
        unlocksCountTextView = (TextView) view.findViewById(R.id.unlocksCountTextView);
    }

    private void updateFragment() {

        long second = TimeHelper.getSeconds(totalUsageTime);
        long minute = TimeHelper.getMinutes(totalUsageTime);
        long hour = TimeHelper.getHour(totalUsageTime);
        int day = TimeHelper.getDay(totalUsageTime);

        String time = String.format("%02d:%02d:%02d:%02d", day, hour, minute, second);
        listAdapter = new WorkTimeStatsListAdapter(getActivity(), appsList);
        listView.setAdapter(listAdapter);
        appUsageStatsCaptionTextView.setText(getString(R.string.usage_time_text_view_caption));
        appUsageStatsTextView.setText(time);
        unlocksCountTextView.setText(Integer.toString(unlocksCount));
    }
}
