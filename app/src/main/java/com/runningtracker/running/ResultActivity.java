package com.runningtracker.running;

import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.runningtracker.running.model.TrackingHistoryCallback;

import runningtracker.R;
import com.runningtracker.common.InitializationFirebase;
import com.runningtracker.data.model.running.ResultObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private static PresenterRunning presenterRunning;
    public static ArrayList<ViewGroup> tabFragmentLayouts;
    public static String idDateHistory;
    private static FirebaseFirestore firestore;


    public ResultActivity() {
        tabFragmentLayouts = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenterRunning = new PresenterRunning();
        /**
         * set value tab result
         * */
        setContentView(R.layout.activity_result);
        initializeUI();
        /**
         * Create firebase and userID
         * */
        InitializationFirebase initializationFirebase = new InitializationFirebase();
        firestore = initializationFirebase.createFirebase();

        StatsTabFragment.setStatsValue(getIntent());


    }

    private void initializeUI() {
        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        /**event back dashboard*/
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (idDateHistory  == null) {
//                    Intent intent0 = new Intent(ResultActivity.this, RunningActivity.class);
//                    startActivity(intent0);
//                } else {
//                    finish();
//                }
                onBackPressed();
            }
        });

        // ViewPager support swiping between tabs
        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Tabs
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons(tabLayout);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new StatsTabFragment(), "STATS");
        adapter.addFragment(new TrackTabFragment(), "TRACK");
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons(TabLayout tabLayout) {
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_map_result);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_graph_result);
    }


    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private ArrayList<Fragment> fragmentList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentTitleList.add(title);
            fragmentList.add(fragment);
        }
    }

    public static class StatsTabFragment extends Fragment {
        private static String mDuration;
        private static float mAvgPace, mNetCalorie, mDistance, mMaxPace, mGrossCalorie;
        private static HashMap<Integer, View> childViews;
        private static TextView txtDuration, txtDistance, txtAvgPace, txtMaxPace, txtAvgSpeed,
                txtMaxSpeed, txtNetCalorie, txtGrossCalorie;

        public StatsTabFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //Inflate the gridview_dashboard_item for this fragment
            ViewGroup inflatedLayout = (ViewGroup) inflater.inflate(R.layout.activity_result_tab_stats, container, false);
            txtDuration = inflatedLayout.findViewById(R.id.textValueDuration);
            txtDistance = inflatedLayout.findViewById(R.id.textValueDistance);
            txtAvgPace = inflatedLayout.findViewById(R.id.textValueAveragePace);
            txtMaxPace = inflatedLayout.findViewById(R.id.textValueMaxPace);
            txtAvgSpeed = inflatedLayout.findViewById(R.id.textValueAverageSpeed);
            txtMaxSpeed = inflatedLayout.findViewById(R.id.textValueMaxSpeed);
            txtNetCalorie = inflatedLayout.findViewById(R.id.textValueNetCalorie);
            txtGrossCalorie = inflatedLayout.findViewById(R.id.textValueGrossCalorie);
            tabFragmentLayouts.add(inflatedLayout);

            assignValueToView();

            return inflatedLayout;
        }

        static void setStatsValue(Intent intent) {
            idDateHistory = intent.getStringExtra("idDate");
            if (idDateHistory == null) {
                mDuration = intent.getStringExtra("duration");
                mAvgPace = intent.getFloatExtra("avgPace", 0);
                mNetCalorie = intent.getFloatExtra("netCalorie", 0);
                mDistance = intent.getFloatExtra("distance", 0);
                mMaxPace = intent.getFloatExtra("maxPace", 0);
                mGrossCalorie = intent.getFloatExtra("grossCalorie", 0);
            } else {
                presenterRunning.getTrackingHistory(idDateHistory, firestore, new TrackingHistoryCallback() {
                    @Override
                    public void onSuccessTrackingData(List<ResultObject> resultObject) {
                        mDuration = resultObject.get(0).getDuration();
                        mAvgPace = resultObject.get(0).getPace();
                        mNetCalorie = resultObject.get(0).getNetCalorie();
                        mDistance = resultObject.get(0).getDistance();
                        mMaxPace = resultObject.get(0).getMaxPace();
                        mGrossCalorie = resultObject.get(0).getGrossCalorie();
                        /**
                         * set value to view
                         * */
                        assignValueToView();
                    }
                });
            }
        }

        private static void assignValueToView() {


            txtDuration.setText(mDuration);
            txtDistance.setText(Float.toString(mDistance));
            txtAvgPace.setText(Float.toString(mAvgPace));
            txtMaxPace.setText(Float.toString(mMaxPace));
            if (mAvgPace > 0.0 && mMaxPace > 0.0) {
                txtAvgSpeed.setText(Float.toString(presenterRunning.RoundAvoid(60 / mAvgPace, 2)));
                txtMaxSpeed.setText(Float.toString(presenterRunning.RoundAvoid(60 / mMaxPace, 2)));
            }
            txtNetCalorie.setText(Float.toString(mNetCalorie));
            txtGrossCalorie.setText(Float.toString(mGrossCalorie));
        }
    }
}