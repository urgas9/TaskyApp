package si.uni_lj.fri.taskyapp;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import si.uni_lj.fri.taskyapp.adapter.SimpleTextArrayAdapter;
import si.uni_lj.fri.taskyapp.data.EnvironmentData;
import si.uni_lj.fri.taskyapp.data.SensorReadingData;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;

public class LabelTaskActivity extends AppCompatActivity implements OnMapReadyCallback{

    private final String TAG = "LabelTaskActivity";
    private SensorReadingData mSensorReadingData;

    @Bind(R.id.tv_num_bt_devices)
    TextView mNumBtDevicesTv;
    @Bind(R.id.iv_bt_icon)
    ImageView mBtIcon;
    @Bind(R.id.iv_wifi_icon)
    ImageView mWifiIcon;
    @Bind(R.id.tv_num_wifi_devices)
    TextView mNumWifiDevicesTv;
    @Bind(R.id.tv_detected_activity)
    TextView mDetectedActivityTv;
    @Bind(R.id.spinner_label_task)
    Spinner mLabelTaskSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_task);

        mSensorReadingData = new Gson().fromJson(getIntent().getStringExtra("task"), SensorReadingData.class);

        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        if(mSensorReadingData.getActivityData() != null) {
            mDetectedActivityTv.setText(mSensorReadingData.getActivityData().getActivityType());
        }
        else{
            mDetectedActivityTv.setText(R.string.no_data);
        }

        EnvironmentData environmentData = mSensorReadingData.getEnvironmentData();
        if(environmentData.isBluetoothTurnedOn()){
            mBtIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bluetooth_black_18dp));
            mNumBtDevicesTv.setText(environmentData.getnBluetoothDevicesNearby());
        }
        else{
            mBtIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bluetooth_disabled_black_18dp));
            mNumBtDevicesTv.setVisibility(View.GONE);
        }
        if(environmentData.isWifiTurnedOn()){
            mWifiIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_network_wifi_black_18dp));
            mNumWifiDevicesTv.setText(environmentData.getnWifiDevicesNearby()+"");
        }
        else{
            mWifiIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_signal_wifi_off_black_18dp));
            mNumWifiDevicesTv.setVisibility(View.GONE);
        }
        String[] arrayOfComplexities = getResources().getStringArray(R.array.task_complexities_array);
        arrayOfComplexities[0] = getResources().getString(R.string.complexity_prompt);
        SimpleTextArrayAdapter adapter = new SimpleTextArrayAdapter(this, R.layout.spinner_task_complexity_item, arrayOfComplexities);
        mLabelTaskSpinner.setAdapter(adapter);
        if(mSensorReadingData.getLabel() != null && mSensorReadingData.getLabel() > 0) {
            mLabelTaskSpinner.setSelection(mSensorReadingData.getLabel());
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
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
    protected void onPause() {
        super.onPause();
        Integer label = mLabelTaskSpinner.getSelectedItemPosition();
        if(label > 0) {
            Log.d(TAG, "Updating record in database, setting label to: " + label);
            SensorReadingRecord srr = SensorReadingRecord.findById(SensorReadingRecord.class, mSensorReadingData.getDatabaseId());
            srr.setLabel(label);
            mSensorReadingData.setLabel(label);
            srr.setSensorJsonObject(new Gson().toJson(mSensorReadingData));
            srr.save();
        }
    }

    @OnClick(R.id.btn_discard_task)
    public void discardTask(View v){
        Log.d(TAG, "Discarding task with id " + mSensorReadingData.getDatabaseId());
        SensorReadingRecord.findById(SensorReadingRecord.class, mSensorReadingData.getDatabaseId()).delete();
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
