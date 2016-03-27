package si.uni_lj.fri.taskyapp.sensor;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import si.uni_lj.fri.taskyapp.global.PermissionsHelper;
import si.uni_lj.fri.taskyapp.service.SenseDataIntentService;

/**
 * Created by urgas9 on 10. 01. 2016.
 */
public class SensingInitiator implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = "SensingManager";
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private boolean mPendingAction;


    public SensingInitiator(Context context) {
        super();
        this.mContext = context;
        this.mPendingAction = false;
    }


    public void senseWithDefaultSensingConfiguration() {

        senseOnInterval();
        senseOnLocationChanged();
        senseOnActivityRecognition();

        //AppHelper.setRepeatedNotification(mContext, 0, 13, 20, 22);

    }

    public void senseOnActivityRecognition() {
        if (!isUserParticipating(mContext)){
            return;
        }
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            buildGoogleApiClient().connect();
        } else {
            requestActivityUpdates();
        }
    }

    public void senseOnLocationChanged() {
        if (!isUserParticipating(mContext)){
            return;
        }

        requestLocationUpdates();
    }
    public static boolean isUserParticipating(Context mContext){
        boolean participating = PreferenceManager.getDefaultSharedPreferences(mContext).getString("participate_preference", "0").equals("0");
        if(!participating){
            Log.d(TAG, "User is not participating, don't start sensing.");
        }
        else{
            Log.d(TAG, "User is participating, start sensing.");
        }
        return participating;
    }

    public void senseOnInterval() {
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

    private PendingIntent getSensingServicePendingIntent(SensingPolicy sensingPolicy) {
        return getSensingServicePendingIntent(sensingPolicy, -1);
    }
    // TODO: Change Intent service to Service
    private PendingIntent getSensingServicePendingIntent(SensingPolicy sensingPolicy, Integer userLabel) {
        Intent i = new Intent(mContext, SenseDataIntentService.class);
        if (sensingPolicy == SensingPolicy.INTERVAL) {
            i.putExtra("sensing_policy", sensingPolicy.toString());
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
                .requestActivityUpdates(mGoogleApiClient, Constants.APPROXIMATE_INTERVAL_MILLIS, getSensingServicePendingIntent(SensingPolicy.ACTIVITY_UPDATES))
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

        ((LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE))
                .requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, Constants.MIN_INTERVAL_MILLIS, Constants.LOCATION_MIN_DISTANCE_TO_LAST_LOC, getSensingServicePendingIntent(SensingPolicy.LOCATION_UPDATES));

    }

    private void requestAlarmIntervalUpdates() {
        stopAllUpdates();
        Log.d(TAG, "Firing requested alarm interval updates.");
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                Constants.APPROXIMATE_INTERVAL_MILLIS,
                Constants.APPROXIMATE_INTERVAL_MILLIS,
                getSensingServicePendingIntent(SensingPolicy.INTERVAL));
    }

    /**
     * Method will try to reconnect to GPlay services, or start pending sensing.
     * For example. Requesting location updates cannot be done, while user don't grant
     * ACCESS_FINE_LOCATION permission
     */
    public void checkForPendingActions(SensingPolicy sensingPolicy) {

        if (mPendingAction) {
            switch (sensingPolicy) {
                case LOCATION_UPDATES:
                    requestLocationUpdates();
                    break;
                default:
                    Log.d(TAG, "There appears to be a pending action, policy equals to: " + sensingPolicy);
            }
        }
    }

    /**
     * Stopping all updates, regardless of sensing policy
     */
    public void stopAllUpdates() {
        Log.d(TAG, "Stopping all updates.");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getSensingServicePendingIntent(SensingPolicy.LOCATION_UPDATES));
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, getSensingServicePendingIntent(SensingPolicy.ACTIVITY_UPDATES));
        }
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getSensingServicePendingIntent(SensingPolicy.INTERVAL));
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Cannot connect to Google API client :(");
    }

    @Override
    public void onResult(@NonNull Status status) {
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
