package si.uni_lj.fri.taskyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import si.uni_lj.fri.taskyapp.data.SensorReadingData;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.global.PermissionsHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;
import si.uni_lj.fri.taskyapp.sensor.SensingInitiator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    @Bind(R.id.start_sensing_view_switcher)
    ViewSwitcher mStartSensingViewSwitcher;
    @Bind(R.id.status_tv)
    TextView mCountDownStatusTv;
    @Bind(R.id.countdown_progressbar)
    CircularProgressBar mCountdownProgress;
    @Bind(R.id.countdown_text)
    TextView mProgressTv;
    @Bind(R.id.btn_finished_sensing)
    Button mFinishedSensingBtn;
    @Bind(R.id.task_complexity_seekbar)
    SeekBar mTaskComplexitySeekBar;
    @Bind(R.id.radio_group_time)
    RadioGroup mSelectTimeRadioGroup;
    @Bind(R.id.seekbar_value_text)
    TextView mSeekbarValueTv;

    private BroadcastReceiver mNewSensorRecordReceiver;
    private CountDownTimer mCountdownTimer;
    Integer selectedComplexity = null;
    boolean isCountDownRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (!AppHelper.isPlayServiceAvailable(this)) {
            Snackbar.make(findViewById(R.id.main_coordinator_layout), "Google Play Services are not available! Please install them first.", Snackbar.LENGTH_INDEFINITE).show();
        }

        /** Requesting permissions for Android Marshmallow and above **/
        if(!PermissionsHelper.hasAllRequiredPermissions(this)) {
            Log.d(TAG, "Cannot start sensing, please grant us required permissions.");
            Toast.makeText(this, "Cannot start sensing, please grant us required permissions.", Toast.LENGTH_LONG).show();
            PermissionsHelper.requestAllRequiredPermissions(this);
        }
        else {
            Log.d(TAG, "Starting sensing.");
            broadcastIntentToStartSensing();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_NEW_SENSOR_READING_RECORD);
        mNewSensorRecordReceiver = new SensorRecordReceiver();
        registerReceiver(new SensorRecordReceiver(), filter);

        final String[] arrayOfComplexities = getResources().getStringArray(R.array.task_complexities_array);

        mTaskComplexitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedComplexity = progress;
                mSeekbarValueTv.setText(arrayOfComplexities[progress+1]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @OnClick(R.id.btn_label_data)
    public void startListDataActivity(View v){
        Intent intent = new Intent(this, ListDataActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            unregisterReceiver(mNewSensorRecordReceiver);
        } catch(IllegalArgumentException e){}
    }

    @OnClick(R.id.btn_finished_sensing)
    public void onFinishedSensingBtn(){

        if(isCountDownRunning) {
            mCountdownTimer.cancel();
            mFinishedSensingBtn.setText(R.string.back);
            isCountDownRunning = false;
        }
        else{
            mStartSensingViewSwitcher.setDisplayedChild(0);
        }
    }

    private int getTimeInSecondsFromRadioGroup(){
        String timeString = ((RadioButton)mSelectTimeRadioGroup.findViewById(mSelectTimeRadioGroup.getCheckedRadioButtonId())).getText().toString();
        timeString = timeString.replaceAll("[^\\d.]", "");
        return Integer.parseInt(timeString);
    }
    @OnClick(R.id.btn_start_sensing)
    public void startCountDownForSensing(View v){
        if(selectedComplexity != null) {




            mFinishedSensingBtn.setText(R.string.cancel);
            mStartSensingViewSwitcher.setDisplayedChild(1);

            final int countdownSeconds = getTimeInSecondsFromRadioGroup();

            isCountDownRunning = true;
            mCountdownProgress.setProgress(countdownSeconds);

            final int MAX_VALUE = countdownSeconds;
            mProgressTv.setText(countdownSeconds + "s");
            mCountdownTimer = new CountDownTimer(countdownSeconds * 1000 + 200, 1000) {

                public void onTick(long millisUntilFinished) {
                    int valueInSecs = Math.round(millisUntilFinished / 1000.f);
                    int value = (int)((valueInSecs*100)/(float)MAX_VALUE);
                    Log.d(TAG, "OnTick: " + value + " millisUntilFinished " + millisUntilFinished);
                    mProgressTv.setText(valueInSecs + "s");
                    mCountdownProgress.setProgressWithAnimation(value, 1000);
                }

                public void onFinish() {
                    mCountdownProgress.setProgressWithAnimation(0, 1000);
                    mProgressTv.setText("0s");
                    mFinishedSensingBtn.setText(R.string.back);
                    mFinishedSensingBtn.setVisibility(View.VISIBLE);
                    isCountDownRunning = false;
                    new SensingInitiator(getBaseContext()).startSensingOnUserRequest(selectedComplexity);
                    Toast.makeText(getBaseContext(), "Started labeled (" + selectedComplexity + ") sensing.", Toast.LENGTH_LONG).show();
                }
            }.start();

        }
        else{
            Toast.makeText(this, R.string.task_complexity_not_selected, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                Toast.makeText(this, "TODO: Open settings.", Toast.LENGTH_LONG).show();
                break;
            default:
                Log.d(TAG, "Menu click not handled.");
        }
        return super.onOptionsItemSelected(item);
    }

    class SensorRecordReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long recordId = intent.getLongExtra("id", 0);
            Log.d(TAG, "Received new sensor reading record with id: " + recordId);
            SensorReadingRecord srr = SensorReadingRecord.findById(SensorReadingRecord.class, recordId);
            SensorReadingData mSensorReadingData = new Gson().fromJson(srr.getSensorJsonObject(), SensorReadingData.class);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, final String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult reached");
        switch (requestCode) {
            case PermissionsHelper.REQUEST_ALL_PERMISSIONS_CODE:
                boolean allGranted = true;
                List<String> permissionsRequiredList = Arrays.asList(PermissionsHelper.permissions);
                for (int i = 0; i < permissions.length; i++) {
                    if (permissionsRequiredList.contains(permissions[i]) && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        allGranted = false;
                        break;
                    }
                    //PermissionsHelper.markAsAsked(this, permissions[i]);
                }
                if(allGranted) {
                    Log.d(TAG, "Yeey. All permissions are granted!");
                    broadcastIntentToStartSensing();
                    return;
                }
                else if (PermissionsHelper.shouldShowAnyPermissionRationale(this)) {
                    new MaterialDialog.Builder(MainActivity.this)
                            .content(getString(R.string.permission_sensor_denied))
                            .positiveText(getString(R.string.ok))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                    ActivityCompat.requestPermissions(MainActivity.this, permissions, PermissionsHelper.REQUEST_ALL_PERMISSIONS_CODE);
                                }
                            })
                            .build()
                            .show();
                } else {
                    Snackbar s = Snackbar.make(findViewById(R.id.main_coordinator_layout),
                            R.string.permission_forever_denied, Snackbar.LENGTH_LONG);
                    s.setAction(R.string.action_settings, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PermissionsHelper.openAppOSSettings(MainActivity.this);
                        }
                    });
                    s.show();
                }
                break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void broadcastIntentToStartSensing(){
        Intent keepAliveBroadcastIntent = new Intent();
        keepAliveBroadcastIntent.setAction(Constants.ACTION_KEEP_SENSING_ALIVE);
        sendBroadcast(keepAliveBroadcastIntent);
    }

}
