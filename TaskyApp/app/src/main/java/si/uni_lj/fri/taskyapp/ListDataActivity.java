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
import android.widget.Button;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
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
    @Bind(R.id.btn_show_map)
    Button mShowMapBtn;

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
            mFullScreenMapFragment = (FullScreenMapFragment)getSupportFragmentManager().getFragment(savedInstanceState, "mFullScreenMapContent");
        }
        mFullScreenMapFragment = (FullScreenMapFragment)getSupportFragmentManager().findFragmentById(R.id.full_map_fragment_frame);
        if(mFullScreenMapFragment == null){
            mFullScreenMapFragment = FullScreenMapFragment.newInstance(FullScreenMapFragment.VIEW_MARKERS, false);
            getSupportFragmentManager().beginTransaction().replace(R.id.full_map_fragment_frame, mFullScreenMapFragment).commit();
        }
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mDataRecyclerView.setLayoutManager(llm);
        mDataRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mAdapter = new ListDataRecyclerAdapter(this);
        mDataRecyclerView.setAdapter(mAdapter);
        //Filter the Intent and register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_NEW_SENSOR_READING_RECORD);
        mNewSensorRecordReceiver = new SensorRecordReceiver();
        registerReceiver(new SensorRecordReceiver(), filter);

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
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(mNewSensorRecordReceiver);
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, "mFullScreenMapContent", mFullScreenMapFragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.LABEL_TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            int label = data.getIntExtra("label", -1);
            long taskDbId = data.getLongExtra("db_record_id", -1);
            int action = data.getIntExtra("action", -1);
            mAdapter.updateDatabaseRecord(taskDbId, 0);
            mFullScreenMapFragment.dataWasUpdated(taskDbId);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class ReadAllSensorRecords extends AsyncTask<Void, Void, SensorReadingDataWithSections> {

        @Override
        protected SensorReadingDataWithSections doInBackground(Void... params) {

            Calendar calendar = AppHelper.getCalendarAtMidnight(-1);

            List<SensorReadingRecord> sensorReadings = SensorReadingRecord.find(SensorReadingRecord.class,
                    "time_started_sensing > ? AND label <= 0", new String[]{"" + calendar.getTimeInMillis()}, null, "time_started_sensing ASC", null);

            ArrayList<SensorReadingRecord> resultList = new ArrayList<>();
            HashSet<String> uniqueDays = new HashSet<>();
            SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT_TO_SHOW_DAY, Locale.ENGLISH);
            String previousTimestampDay = null;
            for (SensorReadingRecord srr : sensorReadings) {
                String dayTimestamp = format.format(new Date(srr.getTimeStartedSensing()));
                if (!dayTimestamp.equals(previousTimestampDay)) {
                    resultList.add(null); // HeaderItem to start a new section
                }
                uniqueDays.add(dayTimestamp);

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
                resultList.add(srr);
            }

            return new SensorReadingDataWithSections(uniqueDays.size(), resultList);
        }

        @Override
        protected void onPostExecute(SensorReadingDataWithSections resultData) {
            super.onPostExecute(resultData);

            if(resultData == null || resultData.getDataList() == null || resultData.getDataList().size()<=1){
                mLoadingViewSwitcher.setDisplayedChild(0);
                mListDataStatusViewSwitcher.setDisplayedChild(1);
                return;
            }
            else {
                mLoadingViewSwitcher.setDisplayedChild(1);
            }

            ArrayList<MarkerDataHolder> markerDataHolderArrayList = new ArrayList<>();
            for(SensorReadingRecord srr : resultData.getDataList()){
                if(srr != null) {
                    markerDataHolderArrayList.add(srr.getMarkerDataHolder());
                }
            }

            mFullScreenMapFragment.setDataList(markerDataHolderArrayList);
            mAdapter.setAdapterData(resultData);
            //mStatusTextView.setText(s);
        }
    }

    @OnClick(R.id.btn_show_list)
    public void showListBtnClicked(View v){
        mLoadingViewSwitcher.setDisplayedChild(1);
    }

    @OnClick(R.id.btn_show_map)
    public void showMapBtnClicked(View v){
        mLoadingViewSwitcher.setDisplayedChild(2);
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
