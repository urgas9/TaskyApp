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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import si.uni_lj.fri.taskyapp.global.PermissionsHelper;
import si.uni_lj.fri.taskyapp.service.ScreenStateService;
import si.uni_lj.fri.taskyapp.service.SenseDataIntentService;

/**
 * Created by urgas9 on 10. 01. 2016.
 */
public class SensingInitiator implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = "SensingManager";
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private SensingPolicy mWhichPolicy = SensingPolicy.NONE; // 0 = interval, 1 = activity changed, 2 = location changed
    private boolean mPendingAction;


    public SensingInitiator(Context context) {
        super();
        this.mContext = context;
        this.mWhichPolicy = SensingPolicy.NONE;
        this.mPendingAction = false;
        // Starting service to monitor screen state
        context.startService(new Intent(context, ScreenStateService.class));
    }


    public void senseWithDefaultSensingConfiguration() {

        senseOnActivityRecognition();
        //senseOnLocationChanged();
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

    public void startSensingOnUserRequest(Integer userLabel) {
        Log.d(TAG, "startSensingOnUserRequest");
        //getSensingServicePendingIntent(userLabel);

        Intent i = new Intent(mContext, SenseDataIntentService.class);
        i.putExtra("user_label", userLabel);
        mContext.startService(i);
    }

    private GoogleApiClient buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(ActivityRecognition.API)
                        //.addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        return mGoogleApiClient;
    }

    @Override
    public void onConnected(Bundle bundle) {
        //stopAllUpdates();
        /*switch (mWhichPolicy) {
            case ACTIVITY_UPDATES:
                requestActivityUpdates();
                break;
            case LOCATION_UPDATES:
                requestLocationUpdates();
                break;
            default:
                Log.e(TAG, "Connected to Google Play services, but requested policy is not handled, code: " + mWhichPolicy);
        }*/
        // Start sensing
        senseWithDefaultSensingConfiguration();
    }

    private PendingIntent getSensingServicePendingIntent() {
        return getSensingServicePendingIntent(-1);
    }

    private PendingIntent getSensingServicePendingIntent(Integer userLabel) {
        Intent i = new Intent(mContext, SenseDataIntentService.class);
        if (mWhichPolicy == SensingPolicy.INTERVAL) {
            i.putExtra("sensing_policy", mWhichPolicy.toString());
        }
        if (userLabel > 0) {
            i.putExtra("user_label", userLabel);
        }
        return PendingIntent
                .getService(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void requestActivityUpdates() {
        Log.d(TAG, "Starting requested activity recognition updates.");
        stopAllUpdates();
        ActivityRecognition
                .ActivityRecognitionApi
                .requestActivityUpdates(mGoogleApiClient, Constants.APPROXIMATE_INTERVAL_MILLIS, getSensingServicePendingIntent())
                .setResultCallback(this);
    }

    private void requestLocationUpdates() {
        if (!PermissionsHelper.hasPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "No location permissions provided.");
            mPendingAction = true;
            return;
        }
        stopAllUpdates();
        Log.d(TAG, "Firing requested location updates.");
        LocationRequest myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(Constants.APPROXIMATE_INTERVAL_MILLIS);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, myLocationRequest, getSensingServicePendingIntent());
    }

    private void requestAlarmIntervalUpdates() {
        stopAllUpdates();
        Log.d(TAG, "Firing requested alarm interval updates.");
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                Constants.APPROXIMATE_INTERVAL_MILLIS,
                Constants.APPROXIMATE_INTERVAL_MILLIS,
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

    /**
     * Stopping all updates, regardless of sensing policy
     */
    public void stopAllUpdates() {
        Log.d(TAG, "Stopping all updates.");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getSensingServicePendingIntent());
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, getSensingServicePendingIntent());
        }
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getSensingServicePendingIntent());
    }

    public void dispose() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.d(TAG, "Connection to Google API client failed for some reason, trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Cannot connect to Google API client :(");
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.d(TAG, "Successfully started or remove updates of Google API client.");
        } else {
            Log.e(TAG, "Could not start or remove updates from Google API client.");
        }
    }

    enum SensingPolicy {
        NONE, LOCATION_UPDATES, INTERVAL, ACTIVITY_UPDATES
    }
}
