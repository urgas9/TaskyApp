package si.uni_lj.fri.taskyapp;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ViewSwitcher;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import si.uni_lj.fri.taskyapp.adapter.DividerItemDecoration;
import si.uni_lj.fri.taskyapp.adapter.ListDataRecyclerAdapter;
import si.uni_lj.fri.taskyapp.broadcast_receivers.NewSensorReadingReceiver;
import si.uni_lj.fri.taskyapp.data.SensorReadingData;
import si.uni_lj.fri.taskyapp.data.SensorReadingDataWithSections;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.sensor.Constants;


// Activity recognition android: http://tutsberry.com/activity-recognition-implementation-on-android/
public class ListDataActivity extends AppCompatActivity {

    private static final String TAG = "ListResultsActivity";
    BroadcastReceiver newSensorReadingReceiver;

    @Bind(R.id.list_data_recycler_view)
    RecyclerView mDataRecyclerView;
    @Bind(R.id.list_data_view_switcher)
    ViewSwitcher mLoadingViewSwitcher;

    ListDataRecyclerAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_data);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLoadingViewSwitcher.setDisplayedChild(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newSensorReadingReceiver = new NewSensorReadingReceiver();
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mDataRecyclerView.setLayoutManager(llm);
        mDataRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mAdapter = new ListDataRecyclerAdapter(getBaseContext());
        mDataRecyclerView.setAdapter(mAdapter);
        //Filter the Intent and register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_NEW_SENSOR_READING);
        registerReceiver(newSensorReadingReceiver, filter);

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
    protected void onDestroy() {
        super.onDestroy();

        //Disconnect and detach the receiver
        unregisterReceiver(newSensorReadingReceiver);
    }

    class ReadAllSensorRecords extends AsyncTask<Void, Void, SensorReadingDataWithSections>{

        @Override
        protected SensorReadingDataWithSections doInBackground(Void... params) {
            List<SensorReadingRecord> sensorReadings = SensorReadingRecord.listAll(SensorReadingRecord.class, "time_saved ASC");
            ArrayList<SensorReadingData> dataList = new ArrayList<>();
            HashSet<String> uniqueDays = new HashSet<>();
            SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT_TO_SHOW_DAY, Locale.ENGLISH);
            Gson gson = new Gson();
            SensorReadingData srd;
            String previousTimestampDay = null;
            for(SensorReadingRecord srr : sensorReadings){
                srd = gson.fromJson(srr.getSensorJsonObject(), SensorReadingData.class);
                String dayTimestamp = format.format(new Date(srd.getTimestampStarted()));
                if(!dayTimestamp.equals(previousTimestampDay)){
                    dataList.add(null); // HeaderItem to start a new section
                }
                uniqueDays.add(dayTimestamp);
                srd.setDatabaseId(srr.getId());
                previousTimestampDay = dayTimestamp;
                dataList.add(srd);
            }

            return new SensorReadingDataWithSections(uniqueDays.size(), dataList);
        }

        @Override
        protected void onPostExecute(SensorReadingDataWithSections resultData) {
            super.onPostExecute(resultData);

            mAdapter.setAdapterData(resultData);
            mLoadingViewSwitcher.setDisplayedChild(1);
            //mStatusTextView.setText(s);
        }
    }

}
