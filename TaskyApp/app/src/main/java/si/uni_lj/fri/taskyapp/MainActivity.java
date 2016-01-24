package si.uni_lj.fri.taskyapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import si.uni_lj.fri.taskyapp.broadcast_receivers.NewSensorReadingReceiver;
import si.uni_lj.fri.taskyapp.data.SensorReadingData;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.global.PermissionsHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;
import si.uni_lj.fri.taskyapp.sensor.SensingInitiator;


// Activity recognition android: http://tutsberry.com/activity-recognition-implementation-on-android/
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    BroadcastReceiver newSensorReadingReceiver;
    SensingInitiator mSensingInitiator;

    @Bind(R.id.app_status_tv)
    TextView mStatusTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStatusTextView.setText("Launching main activity.");
        if (!AppHelper.isPlayServiceAvailable(this)) {
            Snackbar.make(findViewById(R.id.main_coordinator_layout), "Google Play Services are not available! Please install them first.", Snackbar.LENGTH_INDEFINITE).show();
        }

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        */
        /** Requesting permissions for Android Marshmallow and above **/
        // TODO: Handle this better
        PermissionsHelper.requestAllRequiredPermissions(this);

        mSensingInitiator = new SensingInitiator(this);
        //mSensingInitiator.senseOnInterval();
        mSensingInitiator.senseWithDefaultSensingConfiguration();
        //mSensingInitiator.senseOnLocationChanged();

        newSensorReadingReceiver = new NewSensorReadingReceiver(mStatusTextView);

        //Filter the Intent and register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.NEW_SENSOR_READING_ACTION);
        registerReceiver(newSensorReadingReceiver, filter);

        new ReadAllSensorRecords().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mSensingInitiator != null) {
            mSensingInitiator.dispose();
        }
        //Disconnect and detach the receiver
        unregisterReceiver(newSensorReadingReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, final String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult reached");
        switch (requestCode) {
            case PermissionsHelper.REQUEST_LOCATION_PERMISSIONS_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        mSensingInitiator.checkForPendingActions();
                    }
                    PermissionsHelper.markAsAsked(this, permissions[i]);
                }

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    new MaterialDialog.Builder(MainActivity.this)
                            .content(getString(R.string.permission_location_denied))
                            .positiveText(getString(R.string.ok))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                    ActivityCompat.requestPermissions(MainActivity.this, permissions, PermissionsHelper.REQUEST_WRITE_STORAGE_PERMISSIONS_CODE);
                                }
                            })
                            .build()
                            .show();
                } else {
                    Snackbar s = Snackbar.make(findViewById(R.id.main_coordinator_layout),
                            R.string.permission_location_forever_denied, Snackbar.LENGTH_LONG);
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
    }

    class ReadAllSensorRecords extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... params) {
            List<SensorReadingRecord> sensorReadings = SensorReadingRecord.listAll(SensorReadingRecord.class);
            StringBuilder sb = new StringBuilder();
            Gson gson = new Gson();
            SensorReadingData rec;
            sb.append("All records found: ").append(sensorReadings.size()).append("\n");
            for(SensorReadingRecord srr : sensorReadings){
                rec = gson.fromJson(srr.getSensorJsonObject(), SensorReadingData.class);
                String time = new java.text.SimpleDateFormat("dd/MM HH:mm:ss").format(new Date(rec.getTimestampStarted()));
                sb.append(time).append(": ").append(rec.getActivityData());
                sb.append("\n");
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mStatusTextView.setText(s);
        }
    }

}
