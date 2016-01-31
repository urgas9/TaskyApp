package si.uni_lj.fri.taskyapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.ButterKnife;
import butterknife.OnClick;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.global.PermissionsHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        if (!AppHelper.isPlayServiceAvailable(this)) {
            Snackbar.make(findViewById(R.id.main_coordinator_layout), "Google Play Services are not available! Please install them first.", Snackbar.LENGTH_INDEFINITE).show();
        }

        /** Requesting permissions for Android Marshmallow and above **/
        // TODO: Handle this better
        PermissionsHelper.requestAllRequiredPermissions(this);

        broadcastIntentToStartSensing();

    }

    @OnClick(R.id.btn_label_data)
    public void startLabelDataActivity(View v){
        Intent intent = new Intent(this, ListDataActivity.class);
        startActivity(intent);

        Toast.makeText(this, "Started LabelDataActivtiy.", Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.layout_start_sensing)
    public void startLabeledSensing(View v){
        broadcastIntentToStartSensing();
        Toast.makeText(this, "Started labeled sensing.", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, final String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult reached");
        switch (requestCode) {
            case PermissionsHelper.REQUEST_LOCATION_PERMISSIONS_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        broadcastIntentToStartSensing();
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

    private void broadcastIntentToStartSensing(){
        Intent keepAliveBroadcastIntent = new Intent();
        keepAliveBroadcastIntent.setAction(Constants.ACTION_KEEP_SENSING_ALIVE);
        sendBroadcast(keepAliveBroadcastIntent);
    }

}
