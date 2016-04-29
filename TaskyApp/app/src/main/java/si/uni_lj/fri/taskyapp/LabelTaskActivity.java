package si.uni_lj.fri.taskyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import si.uni_lj.fri.taskyapp.data.EnvironmentData;
import si.uni_lj.fri.taskyapp.data.SensorReadingData;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.global.CalendarHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;

public class LabelTaskActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final String TAG = "LabelTaskActivity";
    @Bind(R.id.tv_detected_ambient_sound_level)
    TextView mDetectedAmbientSoundLevel;
    @Bind(R.id.tv_task_time_sensed)
    TextView mTimeSensedTv;
    @Bind(R.id.tv_detected_activity)
    TextView mDetectedActivityTv;
    @Bind(R.id.tv_detected_ambient_light_level)
    TextView mDetectedAmbientLightTv;
    @Bind(R.id.task_complexity_seekbar)
    SeekBar mTaskComplexitySeekBar;
    @Bind(R.id.seekbar_value_text)
    TextView mSeekbarValueTv;
    @Bind(R.id.no_location_data_tv)
    TextView mNoLocationDataTv;
    @Bind(R.id.calendar_event_layout)
    LinearLayout mCalendarEventLayout;
    @Bind(R.id.tv_event_on_calendar)
    TextView mCalendarEventNameTv;

    Integer selectedTaskLabel = null;
    String[] arrayOfComplexities;
    private SensorReadingData mSensorReadingData;
    private long mDbRecordId;
    private boolean mFromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_task);

        //mSensorReadingData = new Gson().fromJson(getIntent().getStringExtra("task"), SensorReadingData.class);

        mDbRecordId = getIntent().getLongExtra("db_record_id", 0);
        mFromNotification = getIntent().getBooleanExtra("from_notification", false);

        SensorReadingRecord srr = SensorReadingRecord.findById(SensorReadingRecord.class, mDbRecordId);
        mSensorReadingData = new Gson().fromJson(srr.getSensorJsonObject(), SensorReadingData.class);

        arrayOfComplexities = getResources().getStringArray(R.array.task_difficulties_array);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        if (mSensorReadingData.getActivityData() != null) {
            mDetectedActivityTv.setText(mSensorReadingData.getActivityData().getActivityType());
        } else {
            mDetectedActivityTv.setText(R.string.no_data);
        }

        String timeSpan = DateUtils.getRelativeTimeSpanString(srr.getTimeStartedSensing(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        int minutesDifference = (int) (System.currentTimeMillis() - srr.getTimeStartedSensing()) / (1000 * 60);
        if (minutesDifference < 60) {
            mTimeSensedTv.setText(timeSpan);
        } else {
            SimpleDateFormat formatFullDate = new SimpleDateFormat(Constants.DATE_FORMAT_TO_SHOW_FULL);
            mTimeSensedTv.setText(formatFullDate.format(new Date(srr.getTimeStartedSensing())));
        }
        EnvironmentData environmentData = mSensorReadingData.getEnvironmentData();
        mDetectedAmbientLightTv.setText("" + environmentData.getAmbientLightData());
        mDetectedAmbientSoundLevel.setText("" + mSensorReadingData.getMicrophoneData().getMeanAmplitude());

        mSeekbarValueTv.setText(R.string.no_value_chosen);
        resetSeekBar();
        if (srr.getLabel() != null && srr.getLabel() > 0 && srr.getLabel() <= 5) {
            setSeekBarValueText(srr.getLabel());
        }

        mTaskComplexitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setSeekBarValueText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mTaskComplexitySeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setSeekBarValueText(mTaskComplexitySeekBar.getProgress());
                return false;
            }
        });

        if (mSensorReadingData.getLocationData().getLng() == 0.0 && mSensorReadingData.getLocationData().getLat() == 0.0) {
            mNoLocationDataTv.setVisibility(View.VISIBLE);
        }

        String eventName = CalendarHelper.getEventNameAtTime(this, mSensorReadingData.getTimestampStarted());
        if (eventName != null) {
            mCalendarEventLayout.setVisibility(View.VISIBLE);
            mCalendarEventNameTv.setText(eventName);
        } else {
            mCalendarEventLayout.setVisibility(View.GONE);
        }


        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setSeekBarValueText(int progress) {
        selectedTaskLabel = progress + 1;
        mTaskComplexitySeekBar.setProgress(progress);
        mSeekbarValueTv.setText(arrayOfComplexities[progress + 1]);
        mSeekbarValueTv.setTextColor(ContextCompat.getColor(this, R.color.secondary_text));
    }

    private void resetSeekBar() {
        mSeekbarValueTv.setText(R.string.no_value_chosen);
        mTaskComplexitySeekBar.setProgress(0);
        selectedTaskLabel = null;
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

    @OnClick({R.id.task_complexity_info_iv, R.id.seekbar_value_text})
    public void showInfoDialog(View v) {
        AppHelper.showExplainNotificationsDialog(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*Integer label = mLabelTaskSpinner.getSelectedItemPosition();
        if(label > 0) {
            Log.d(TAG, "Updating record in database, setting label to: " + label);
            SensorReadingRecord srr = SensorReadingRecord.findById(SensorReadingRecord.class, mSensorReadingData.getDatabaseId());
            srr.setLabel(label);
            mSensorReadingData.setLabel(label);
            srr.setSensorJsonObject(new Gson().toJson(mSensorReadingData));
            srr.save();
        }*/
    }


    @OnClick(R.id.btn_label_task_confirm)
    public void confirmTaskLabel(View v) {

        if (selectedTaskLabel != null && selectedTaskLabel > 0) {
            Log.d(TAG, "Updating record in database, setting label to: " + selectedTaskLabel);
            SensorReadingRecord srr = SensorReadingRecord.findById(SensorReadingRecord.class, mDbRecordId);
            srr.setLabel(selectedTaskLabel);
            mSensorReadingData.setLabel(selectedTaskLabel);
            srr.setSensorJsonObject(new Gson().toJson(mSensorReadingData));
            srr.save();
            Intent resultData = new Intent();
            resultData.putExtra("label", selectedTaskLabel);
            resultData.putExtra("db_record_id", mDbRecordId);
            resultData.putExtra("action", 1);
            setResult(RESULT_OK, resultData);
            finish();
        } else {
            Toast.makeText(this, "Please choose task label first.", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_label_task_discard)
    public void discardTask(View v) {
        Log.d(TAG, "Discarding task with id " + mDbRecordId);
        SensorReadingRecord.findById(SensorReadingRecord.class, mDbRecordId).delete();
        Intent data = new Intent();
        data.putExtra("action", 0);
        data.putExtra("db_record_id", mDbRecordId);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(mSensorReadingData.getLocationData().getLat(), mSensorReadingData.getLocationData().getLng());
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(mSensorReadingData.getLocationData().getPrettyLocationString()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }
}
