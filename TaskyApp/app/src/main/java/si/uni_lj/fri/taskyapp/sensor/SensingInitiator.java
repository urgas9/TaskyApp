/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This library was developed as part of the paper submitted for the UbitTention workshop paper (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package si.uni_lj.fri.taskyapp.sensor;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;

import si.uni_lj.fri.taskyapp.global.PermissionsHelper;
import si.uni_lj.fri.taskyapp.global.SensingPolicy;
import si.uni_lj.fri.taskyapp.service.SenseDataIntentService;

/**
 * Created by urgas9 on 10. 01. 2016.
 */
public class SensingInitiator implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = "SensingInitiator";
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;

    public SensingInitiator(Context context) {
        super();
        this.mContext = context;
    }

    public static boolean isUserParticipating(Context mContext, boolean forcedByUser) {
        boolean participating = PreferenceManager.getDefaultSharedPreferences(mContext).getString("participate_preference", "0").equals("0");
        if (!participating) {
            if (forcedByUser) {
                Log.d(TAG, "Sensing forced by user, although user is not participating start sensing.");
            } else {
                Log.d(TAG, "User is not participating, don't start sensing.");
            }

        } else {
            Log.d(TAG, "User is participating, start sensing.");
        }
        return forcedByUser || participating;
    }

    public void senseWithDefaultSensingConfiguration() {
        if (!isUserParticipating(mContext, false)) {
            return;
        }
        Log.d(TAG, "Sensing with default sensing configuration.");
        senseOnInterval();
        senseOnLocationChanged();
        senseOnActivityRecognition();
    }

    public void senseOnActivityRecognition() {
        if (!isUserParticipating(mContext, false)) {
            return;
        }
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            buildGoogleApiClient().connect();
        } else {
            requestActivityUpdates();
        }
    }

    public void senseOnLocationChanged() {
        if (!isUserParticipating(mContext, false)) {
            return;
        }

        requestLocationUpdates();
    }

    public void senseOnInterval() {
        if (!isUserParticipating(mContext, false)) {
            return;
        }
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

    private PendingIntent getSensingServicePendingIntent(SensingPolicy sensingPolicy, Integer userLabel) {
        Intent i = new Intent(mContext, SenseDataIntentService.class);
        if (sensingPolicy == SensingPolicy.INTERVAL) {
            i.putExtra("sensing_policy", sensingPolicy.toString());
        }
        if (userLabel > 0) {
            i.putExtra("user_label", userLabel);
        }
        int requestCode = 0;
        switch (sensingPolicy) {
            case ACTIVITY_UPDATES:
                requestCode = Constants.REQUEST_CODE_ACTIVITY_UPDATES;
                break;
            case LOCATION_UPDATES:
                requestCode = Constants.REQUEST_CODE_LOCATION_UPDATES;
                break;
            case INTERVAL:
                requestCode = Constants.REQUEST_CODE_ALARM_UPDATES;
                break;
        }
        return PendingIntent
                .getService(mContext, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void requestActivityUpdates() {
        Log.d(TAG, "Starting requested activity recognition updates.");
        ActivityRecognition
                .ActivityRecognitionApi
                .requestActivityUpdates(mGoogleApiClient, Constants.APPROXIMATE_INTERVAL_MILLIS, getSensingServicePendingIntent(SensingPolicy.ACTIVITY_UPDATES))
                .setResultCallback(this);
    }

    private void requestLocationUpdates() {
        if (!PermissionsHelper.hasPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "No location permissions provided.");
            return;
        }
        Log.d(TAG, "Firing requested location updates.");

        ((LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE))
                .requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, Constants.MIN_INTERVAL_MILLIS, Constants.LOCATION_MIN_DISTANCE_TO_LAST_LOC, getSensingServicePendingIntent(SensingPolicy.LOCATION_UPDATES));

    }

    private void requestAlarmIntervalUpdates() {
        stopAlarmUpdates();
        Log.d(TAG, "Firing requested alarm interval updates.");
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + Constants.APPROXIMATE_INTERVAL_MILLIS,
                Constants.APPROXIMATE_INTERVAL_MILLIS,
                getSensingServicePendingIntent(SensingPolicy.INTERVAL));
    }

    /**
     * Stopping all updates, regardless of sensing policy
     */
    public void stopAlarmUpdates() {
        Log.d(TAG, "Stopping alarm updates.");
        /*if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getSensingServicePendingIntent(SensingPolicy.LOCATION_UPDATES));
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, getSensingServicePendingIntent(SensingPolicy.ACTIVITY_UPDATES));
        }*/
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getSensingServicePendingIntent(SensingPolicy.INTERVAL));
    }

    public void stopAllUpdates() {
        try {
            Log.d(TAG, "Stopping alarm updates.");
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getSensingServicePendingIntent(SensingPolicy.LOCATION_UPDATES));
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, getSensingServicePendingIntent(SensingPolicy.ACTIVITY_UPDATES));
            }
            stopAlarmUpdates();
        } catch (Exception e) {
            Log.e(TAG, "Cannot stop updates");
        }
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

}
