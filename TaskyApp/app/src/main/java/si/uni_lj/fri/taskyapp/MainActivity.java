package si.uni_lj.fri.taskyapp;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import butterknife.Bind;
import butterknife.ButterKnife;
import si.uni_lj.fri.taskyapp.activity_recognition.ActivityRecognitionIntentService;
import si.uni_lj.fri.taskyapp.broadcast_receivers.NewSensorReadingReceiver;
import si.uni_lj.fri.taskyapp.global.AppHelper;


// Activity recognition android: http://tutsberry.com/activity-recognition-implementation-on-android/
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "MainActivity";
    private GoogleApiClient mGApiClient;
    BroadcastReceiver newSensorReadingReceiver;

    @Bind(R.id.app_status_tv)
    TextView mStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStatusTextView.setText("Launching main activity");
        if(AppHelper.isPlayServiceAvailable(this)){
            Snackbar.make(findViewById(R.id.main_coordinator_layout), "Google Play Services are not available! Please install them first.", Snackbar.LENGTH_INDEFINITE).show();
        }

        mGApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //Connect to Google API
        mGApiClient.connect();

        newSensorReadingReceiver = new NewSensorReadingReceiver(mStatusTextView);
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        */

        //Filter the Intent and register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("NewSensorReading");
        registerReceiver(newSensorReadingReceiver, filter);
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

        //Disconnect and detach the receiver
        mGApiClient.disconnect();
        unregisterReceiver(newSensorReadingReceiver);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Intent i = new Intent(this, ActivityRecognitionIntentService.class);
        PendingIntent mActivityRecongPendingIntent = PendingIntent
                .getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d(TAG, "connected to ActivityRecognition");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGApiClient, 0, mActivityRecongPendingIntent);

        //Update the TextView
        mStatusTextView.setText("Connected to Google Play Services.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Suspended to ActivityRecognition");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Not connected to ActivityRecognition");

        Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(
                this,
                connectionResult.getErrorCode(),
                1);
        if(errorDialog != null){
            errorDialog.show();
        }
    }
}
