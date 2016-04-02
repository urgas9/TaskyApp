package si.uni_lj.fri.taskyapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RadioGroup;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import si.uni_lj.fri.taskyapp.data.MarkerDataHolder;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;

public class GoogleMapFullScreenActivity extends AppCompatActivity {

    private static String TAG = "GoogleMapFullScreenActivity";
    String[] arrayOfComplexities;

    ArrayList<MarkerDataHolder> mMarkerDataHolderList;
    @Bind(R.id.radio_group_map_data_type)
    RadioGroup mMapDataType;

    FullScreenMapFragment mFullScreenMapFragment;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map_full_screen);

        ButterKnife.bind(this);

        Intent paramsIntent = getIntent();
        if (paramsIntent != null) {
            mMarkerDataHolderList = paramsIntent.getParcelableArrayListExtra("markerDataArray");
        }

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mFullScreenMapFragment = (FullScreenMapFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mFullScreenMapContent");
            getSupportFragmentManager().beginTransaction().replace(R.id.map_content_frame, mFullScreenMapFragment).commit();
        }
        if (mFullScreenMapFragment == null) {
            mFullScreenMapFragment = (FullScreenMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_content_frame);
        }
        if (mFullScreenMapFragment == null) {
            mFullScreenMapFragment = FullScreenMapFragment.newInstance(FullScreenMapFragment.VIEW_HEATMAP, true);
            getSupportFragmentManager().beginTransaction().replace(R.id.map_content_frame, mFullScreenMapFragment).commit();
        }
        mFullScreenMapFragment.setDataList(mMarkerDataHolderList);

        arrayOfComplexities = getResources().getStringArray(R.array.task_difficulties_array);

        mMapDataType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Group map data type...");
                switch (mMapDataType.getCheckedRadioButtonId()) {
                    case R.id.radio_heatmap:
                        mFullScreenMapFragment.showHeatMap(false);
                        break;
                    case R.id.radio_tasks:
                        mFullScreenMapFragment.showLabelMarkers(false);
                        break;
                    default:
                        Log.e(TAG, "Radio button not handled!");
                }
            }
        });
        new GetMapDataForLast2Days().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            mFullScreenMapFragment.dataWasUpdated(taskDbId, label);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class GetMapDataForLast2Days extends AsyncTask<Void, Void, ArrayList<MarkerDataHolder>> {
        @Override
        protected ArrayList<MarkerDataHolder> doInBackground(Void... params) {
            Calendar calendarFrom = AppHelper.getCalendarAtMidnight(-1);
            List<SensorReadingRecord> sensorReadings = SensorReadingRecord.find(SensorReadingRecord.class,
                    "time_started_sensing > ?", new String[]{"" + calendarFrom.getTimeInMillis()}, null, "time_started_sensing ASC", null);

            ArrayList<MarkerDataHolder> resultList = new ArrayList<>();
            for (SensorReadingRecord srr : sensorReadings) {
                if (srr.getLocationLat() == 0 && srr.getLocationLng() == 0) {
                    continue;
                }
                resultList.add(srr.getMarkerDataHolder());
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(ArrayList<MarkerDataHolder> result) {
            super.onPostExecute(result);
            mMarkerDataHolderList = result;
        }
    }
}
