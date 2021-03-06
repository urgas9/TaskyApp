/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This project was developed as part of the paper submitted for the UbitTention workshop (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package si.uni_lj.fri.taskyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import si.uni_lj.fri.taskyapp.adapter.DividerItemDecoration;
import si.uni_lj.fri.taskyapp.adapter.ListDataRecyclerAdapter;
import si.uni_lj.fri.taskyapp.data.MarkerDataHolder;
import si.uni_lj.fri.taskyapp.data.SensorReadingDataWithSections;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.global.SensorsHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;


// Activity recognition android: http://tutsberry.com/activity-recognition-implementation-on-android/
public class ListDataActivity extends AppCompatActivity {

    private static final String TAG = "ListResultsActivity";

    BroadcastReceiver mNewSensorRecordReceiver;

    @Bind(R.id.list_data_recycler_view)
    RecyclerView mDataRecyclerView;
    @Bind(R.id.list_data_view_switcher)
    ViewFlipper mLoadingViewSwitcher;
    @Bind(R.id.list_data_status_viewswitcher)
    ViewSwitcher mListDataStatusViewSwitcher;

    ListDataRecyclerAdapter mAdapter;

    FullScreenMapFragment mFullScreenMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_data);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLoadingViewSwitcher.setDisplayedChild(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mFullScreenMapFragment = (FullScreenMapFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mFullScreenMapContent");
        }
        mFullScreenMapFragment = (FullScreenMapFragment) getSupportFragmentManager().findFragmentById(R.id.full_map_fragment_frame);
        if (mFullScreenMapFragment == null) {
            mFullScreenMapFragment = FullScreenMapFragment.newInstance(FullScreenMapFragment.VIEW_MARKERS, false);
            getSupportFragmentManager().beginTransaction().replace(R.id.full_map_fragment_frame, mFullScreenMapFragment).commit();
        }
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mDataRecyclerView.setLayoutManager(llm);
        mDataRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mAdapter = new ListDataRecyclerAdapter(this);
        mDataRecyclerView.setAdapter(mAdapter);

        new ReadAllSensorRecords().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mNewSensorRecordReceiver);
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Filter the Intent and register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_NEW_SENSOR_READING_RECORD);
        mNewSensorRecordReceiver = new SensorRecordReceiver();
        registerReceiver(mNewSensorRecordReceiver, filter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, "mFullScreenMapContent", mFullScreenMapFragment);
    }

    public void noDataCallback() {
        mLoadingViewSwitcher.setDisplayedChild(0);
        mListDataStatusViewSwitcher.setDisplayedChild(1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.LABEL_TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            int label = data.getIntExtra("label", -1);
            long taskDbId = data.getLongExtra("db_record_id", -1);
            int action = data.getIntExtra("action", -1);
            mAdapter.updateDatabaseRecord(taskDbId, 0);
            mFullScreenMapFragment.dataWasUpdated(taskDbId, -1);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.btn_show_list)
    public void showListBtnClicked(View v) {
        mLoadingViewSwitcher.setDisplayedChild(1);
    }

    @OnClick(R.id.btn_show_map)
    public void showMapBtnClicked(View v) {
        mLoadingViewSwitcher.setDisplayedChild(2);
    }

    class ReadAllSensorRecords extends AsyncTask<Void, Void, SensorReadingDataWithSections> {

        @Override
        protected SensorReadingDataWithSections doInBackground(Void... params) {

            Calendar calendar = AppHelper.getCalendarAtMidnight(-1);

            List<SensorReadingRecord> sensorReadings = SensorReadingRecord.find(SensorReadingRecord.class,
                    "time_started_sensing > ? AND label <= 0", new String[]{"" + calendar.getTimeInMillis()}, null, "time_started_sensing ASC", null);

            HashMap<String, List<SensorReadingRecord>> dailyReadingsMap = new HashMap<>();
            LinkedList<String> dayKeyValues = new LinkedList<>();

            SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT_TO_SHOW_DAY, Locale.ENGLISH);
            String previousTimestampDay = null;
            for (SensorReadingRecord srr : sensorReadings) {
                String dayTimestamp = format.format(new Date(srr.getTimeStartedSensing()));
                addToMap(dailyReadingsMap, dayTimestamp, srr);
                if (!dayTimestamp.equals(previousTimestampDay)) {
                    dayKeyValues.add(dayTimestamp);
                }

                if (srr.getAddress() == null) {
                    srr.setAddress(SensorsHelper.getLocationAddress(getBaseContext(), srr.getLocationLat(), srr.getLocationLng()));
                    if (srr.getAddress() != null) {
                        long prevId = srr.getId();
                        long id = srr.save();
                        Log.d(TAG, "Prev id: " + prevId + " new id: " + id);
                    }
                }
                //srd.setDatabaseId(srr.getId());
                previousTimestampDay = dayTimestamp;
            }
            ArrayList<SensorReadingRecord> resultList = new ArrayList<>();
            int MAX_VALUES = 10;
            // Filter values
            int listSize = dayKeyValues.size();
            for (String s : dayKeyValues) {
                resultList.add(null);
                LinkedList<SensorReadingRecord> srrNewList = new LinkedList<>(dailyReadingsMap.get(s));
                int toIndex = Math.min(srrNewList.size(), MAX_VALUES);
                Collections.shuffle(srrNewList);
                List<SensorReadingRecord> orderedSublist = srrNewList.subList(0, Math.max(toIndex, 0));
                Collections.sort(orderedSublist, new Comparator<SensorReadingRecord>() {
                    @Override
                    public int compare(SensorReadingRecord lhs, SensorReadingRecord rhs) {
                        return (lhs.getTimeStartedSensing() < rhs.getTimeStartedSensing()) ? -1 : 1;
                    }
                });
                resultList.addAll(orderedSublist);
            }

            return new SensorReadingDataWithSections(dailyReadingsMap.size(), resultList);
        }

        private void addToMap(HashMap<String, List<SensorReadingRecord>> hashMap, String key, SensorReadingRecord value) {
            List<SensorReadingRecord> list = new LinkedList<>();
            if (hashMap.get(key) != null) {
                list = hashMap.get(key);
            }
            hashMap.put(key, list);
            list.add(value);
        }

        @Override
        protected void onPostExecute(SensorReadingDataWithSections resultData) {
            super.onPostExecute(resultData);

            if (resultData == null || resultData.getDataList() == null || resultData.getDataList().size() <= 1) {
                mLoadingViewSwitcher.setDisplayedChild(0);
                mListDataStatusViewSwitcher.setDisplayedChild(1);
                return;
            } else {
                mLoadingViewSwitcher.setDisplayedChild(1);
            }

            ArrayList<MarkerDataHolder> markerDataHolderArrayList = new ArrayList<>();
            for (SensorReadingRecord srr : resultData.getDataList()) {
                if (srr != null) {
                    markerDataHolderArrayList.add(srr.getMarkerDataHolder());
                }
            }

            mFullScreenMapFragment.setDataList(markerDataHolderArrayList);
            mAdapter.setAdapterData(resultData);
            //mStatusTextView.setText(s);
        }
    }

    class SensorRecordReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long recordId = intent.getLongExtra("id", 0);
            Log.d(TAG, "Received new sensor reading record with id: " + recordId);
            SensorReadingRecord srr = SensorReadingRecord.findById(SensorReadingRecord.class, recordId);
            //SensorReadingData mSensorReadingData = new Gson().fromJson(srr.getSensorJsonObject(), SensorReadingData.class);
            mAdapter.addNewSensorReadingRecord(srr);
            mLoadingViewSwitcher.setDisplayedChild(1);
        }
    }

}
