package si.uni_lj.fri.taskyapp.sensor;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import si.uni_lj.fri.taskyapp.global.PermissionsHelper;

/**
 * Created by urgas9 on 10. 01. 2016.
 */
public class SensingManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SensingManager";
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private SensingPolicy mWhichPolicy = SensingPolicy.NONE; // 0 = interval, 1 = activity changed, 2 = location changed
    private boolean mPendingAction;
    public SensingManager(Context context) {
        super();
        this.mContext = context;
        this.mWhichPolicy = SensingPolicy.NONE;
        this.mPendingAction = false;
    }

    public void senseOnActivityRecognition() {
        mWhichPolicy = SensingPolicy.ACTIVITY_UPDATES;
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            buildGoogleApiClient().connect();
        } else {
            requestActivityUpdates();
        }
    }

    public void senseOnLocationChanged() {
        mWhichPolicy = SensingPolicy.LOCATION_UPDATES;
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            buildGoogleApiClient().connect();
        } else {
            requestLocationUpdates();
        }
    }

    public void senseOnInterval() {
        mWhichPolicy = SensingPolicy.INTERVAL;
        requestAlarmIntervalUpdates();
    }

    private GoogleApiClient buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(ActivityRecognition.API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        return mGoogleApiClient;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (bundle != null) {
            Log.d(TAG, "onConnected, : " + bundle.toString());
        } else {
            Log.d(TAG, "onConnected, bundle == null");
        }
        switch (mWhichPolicy) {
            case ACTIVITY_UPDATES:
                requestActivityUpdates();
                break;
            case LOCATION_UPDATES:
                requestLocationUpdates();
                break;
            default:
                Log.e(TAG, "Connected to Google Play services, but requested policy is not handled, code: " + mWhichPolicy);
        }
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "connected to ActivityRecognition");
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 0, getSensingServicePendingIntent());
        }
    }

    private PendingIntent getSensingServicePendingIntent() {
        Intent i = new Intent(mContext, SenseDataIntentService.class);
        return PendingIntent
                .getService(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void requestActivityUpdates() {
        Log.d(TAG, "Starting requested activity recognition updates.");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 0, getSensingServicePendingIntent());
    }

    private void requestLocationUpdates() {
        if (!PermissionsHelper.hasPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            Log.d(TAG, "No location permissions provided.");
            mPendingAction = true;
            return;
        }
        Log.d(TAG, "Firing requested location updates.");
        LocationRequest myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(Constants.APPROXIMATE_INTERVAL_MILISECS);
        myLocationRequest.setFastestInterval(Constants.APPROXIMATE_INTERVAL_MILISECS / 2);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, myLocationRequest, getSensingServicePendingIntent());
    }

    private void requestAlarmIntervalUpdates() {
        Log.d(TAG, "Firing requested alarm interval updates.");
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                Constants.APPROXIMATE_INTERVAL_MILISECS,
                Constants.APPROXIMATE_INTERVAL_MILISECS,
                getSensingServicePendingIntent());
    }

    /**
     * Method will try to reconnect to GPlay services, or start pending sensing.
     * For example. Requesting location updates cannot be done, while user don't grant
     * ACCESS_FINE_LOCATION permission
     */
    public void checkForPendingActions() {

        if (mPendingAction) {
            switch (mWhichPolicy) {
                case LOCATION_UPDATES:
                    requestLocationUpdates();
                    break;
                default:
                    Log.d(TAG, "There appears to be a pending action, policy equals to: " + mWhichPolicy);
            }
        }
    }

    public void dispose() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Suspended to ActivityRecognition");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Not connected to ActivityRecognition");

        /*Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(
                mContext,
                connectionResult.getErrorCode(),
                1);
        if(errorDialog != null){
            errorDialog.show();
        }*/
    }

    enum SensingPolicy {
        NONE, LOCATION_UPDATES, INTERVAL, ACTIVITY_UPDATES
    }
}
