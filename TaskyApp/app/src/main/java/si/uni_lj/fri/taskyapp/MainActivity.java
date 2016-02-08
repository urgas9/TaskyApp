package si.uni_lj.fri.taskyapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.global.PermissionsHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;
import si.uni_lj.fri.taskyapp.sensor.SensingInitiator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Bind(R.id.spinner_task_complexity)
    Spinner mTaskComplexitySpinner;

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

    }

    @OnClick(R.id.btn_label_data)
    public void startLabelDataActivity(View v){
        Intent intent = new Intent(this, ListDataActivity.class);
        startActivity(intent);
        Toast.makeText(this, "Started LabelDataActivity.", Toast.LENGTH_LONG).show();

    }

    @OnClick(R.id.btn_start_sensing)
    public void startLabeledSensing(View v){
        if(mTaskComplexitySpinner.getSelectedItemPosition() > 0) {
            new SensingInitiator(this).startSensingOnUserRequest(mTaskComplexitySpinner.getSelectedItemPosition());
            //broadcastIntentToStartSensing(mTaskComplexitySpinner.getSelectedItemPosition());
            Toast.makeText(this, "Started labeled (" + mTaskComplexitySpinner.getSelectedItem() + ")sensing.", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, R.string.task_complexity_not_selected, Toast.LENGTH_LONG).show();
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
