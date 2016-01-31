package si.uni_lj.fri.taskyapp.service;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.SensorDataListener;
import com.ubhave.sensormanager.config.pull.PullSensorConfig;
import com.ubhave.sensormanager.data.SensorData;
import com.ubhave.sensormanager.data.env.LightData;
import com.ubhave.sensormanager.data.pull.AccelerometerData;
import com.ubhave.sensormanager.data.pull.BluetoothData;
import com.ubhave.sensormanager.data.pull.ESBluetoothDevice;
import com.ubhave.sensormanager.data.pull.MicrophoneData;
import com.ubhave.sensormanager.data.pull.WifiData;
import com.ubhave.sensormanager.data.pull.WifiScanResult;
import com.ubhave.sensormanager.data.push.ScreenData;
import com.ubhave.sensormanager.sensors.SensorUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import si.uni_lj.fri.taskyapp.data.ActivityData;
import si.uni_lj.fri.taskyapp.data.EnvironmentData;
import si.uni_lj.fri.taskyapp.data.LocationData;
import si.uni_lj.fri.taskyapp.data.PhoneStatusData;
import si.uni_lj.fri.taskyapp.data.SensorReadingData;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.global.SensingDecisionHelper;
import si.uni_lj.fri.taskyapp.global.SensorsHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;
import si.uni_lj.fri.taskyapp.sensor.SensorCallableGenerator;
import si.uni_lj.fri.taskyapp.sensor.SensorThreadsManager;

/**
 * Created by urgas9 on 31. 12. 2015.
 */
public class SenseDataIntentService extends IntentService implements GoogleApiClient.ConnectionCallbacks, SensorDataListener {
    //LogCat
    private static final String TAG = SenseDataIntentService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;

    private static final int[] SENSOR_IDS = {SensorUtils.SENSOR_TYPE_LIGHT, SensorUtils.SENSOR_TYPE_SCREEN};
    private List<Integer> sensorSubscriptionIds;

    //Data for callbacks
    private long timeSensingStarted;
    private List<PhoneStatusData> phoneStatusDataList;
    private float sumLightValues = 0;
    private float countLightValues = 0;


    public SenseDataIntentService() {
        super("SenseDataIntentService");
    }

    private GoogleApiClient buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(ActivityRecognition.API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        return mGoogleApiClient;
    }

    private Location getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String policy = intent.getStringExtra("sensing_policy");
        Gson gson = new Gson();
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Blocking connect to Google API, so we will have a connected instance in future
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            buildGoogleApiClient().blockingConnect(7, TimeUnit.SECONDS);
        }

        Location sensedLocation = null;
        DetectedActivity detectedActivity = null;
        SensorReadingData srd = new SensorReadingData(getApplicationContext());

        if (ActivityRecognitionResult.hasResult(intent)) {
            //Extract the result from the Response
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            detectedActivity = result.getMostProbableActivity();

            //Get the Confidence and Name of Activity
            int confidence = detectedActivity.getConfidence();
            String mostProbableName = SensorsHelper.getDetectedActivityName(detectedActivity.getType());

            //Fire the intent with activity name & confidence
            Intent i = new Intent(Constants.ACTION_NEW_SENSOR_READING);
            i.putExtra("policy", "activity");
            i.putExtra("activity", mostProbableName);
            i.putExtra("confidence", confidence);

            Log.d(TAG, "Most Probable Name : " + mostProbableName);
            Log.d(TAG, "Confidence : " + confidence);
            //Send Broadcast to be listen in MainActivity
            this.sendBroadcast(i);

        } else if (LocationResult.hasResult(intent)) {
            Log.d(TAG, "Got intent from location update.");

            LocationResult result = LocationResult.extractResult(intent);
            sensedLocation = result.getLastLocation();

            Intent i = new Intent(Constants.ACTION_NEW_SENSOR_READING);

            i.putExtra("policy", "location");
            i.putExtra("location", sensedLocation);

            this.sendBroadcast(i);

        } else if (policy != null && policy.equals("INTERVAL")) {
            Log.d(TAG, "Got intent from fired alarm.");

            Intent i = new Intent(Constants.ACTION_NEW_SENSOR_READING);
            i.putExtra("policy", "alarm");
        } else {
            Log.d(TAG, "Policy unresolved: " + policy);
        }

        // We need to get activity and location instance, as one or another may not exist at this point
        if (sensedLocation == null) {
            sensedLocation = getLastLocation();
        }
        if (detectedActivity == null) {
            // TODO: Detect current activity
            /*PendingResult result = ActivityRecognition.
            ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 0, PendingIntent
                    .getService(getApplicationContext(), 0, null, PendingIntent.FLAG_UPDATE_CURRENT));
            Result r = result.await();*/
        }

        timeSensingStarted = System.currentTimeMillis();
        srd.setTimestampStarted(timeSensingStarted);

        if (sensedLocation != null) {
            srd.setLocationData(new LocationData(sensedLocation));
        }
        if (detectedActivity != null) {
            srd.setActivityData(new ActivityData(detectedActivity));
        }

        SensingDecisionHelper sensingHelper = new SensingDecisionHelper(getApplicationContext());
        if(!sensingHelper.shouldContinueSensing(srd)){
            Log.d(TAG, "Decided not to sense this time!");
            return;
        }
        Log.d(TAG, "Decided to start sensing.");
        sensingHelper.saveNewDecisiveSensingData(srd);

        final ESSensorManager sm;
        try {
            sm = ESSensorManager.getSensorManager(getApplicationContext());
            sm.setSensorConfig(SensorUtils.SENSOR_TYPE_ACCELEROMETER,
                    PullSensorConfig.SENSE_WINDOW_LENGTH_MILLIS, Constants.SENSING_WINDOW_LENGTH_MILLIS);
            sm.setSensorConfig(SensorUtils.SENSOR_TYPE_MICROPHONE, PullSensorConfig.SENSE_WINDOW_LENGTH_MILLIS, Constants.SENSING_WINDOW_LENGTH_MILLIS);

        } catch (ESException e) {
            e.printStackTrace();
            Log.e(TAG, "Cannot start sensing due to an exception, message: " + e.getMessage());
            return;
        }
        sensorSubscriptionIds = new LinkedList<>();

        SensorThreadsManager sensorThreadsManager = new SensorThreadsManager();

        sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_ACCELEROMETER));
        sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_BLUETOOTH));
        //sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_LIGHT));
        //sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_SCREEN));
        //sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_AMBIENT_TEMPERATURE));
        sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_MICROPHONE));
        //sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_SMS));
        sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_WIFI));
        Log.d(TAG, "Threads submitted");

        // Subscribing to push sensors
        subscribeToSensors(sm);
        Log.d(TAG, "Subscribed to sensors.");
        Log.d(TAG, "Now, trying to get sensor results.");

        Future futureSensedData;
        EnvironmentData environmentData = new EnvironmentData();
        while (sensorThreadsManager.moreResultsAvailable()) {
            Object sensingData;
            try {
                futureSensedData = sensorThreadsManager.take(); // Blocking call to get the next Future result
                sensingData = futureSensedData.get();

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                continue;
            }
            if (sensingData instanceof BluetoothData) {
                ArrayList<ESBluetoothDevice> bDevices = ((BluetoothData) sensingData).getBluetoothDevices();
                Log.d(TAG, "Got nearby bluetooth devices.");
                for (ESBluetoothDevice esbd : bDevices) {
                    Log.d(TAG, esbd.getBluetoothDeviceName());
                }
                environmentData.setnBluetoothDevicesNearby(bDevices.size());
            } else if (sensingData instanceof AccelerometerData) {
                float[] meanValues = SensorsHelper.getMeanAccelerometerValues(((AccelerometerData) sensingData).getSensorReadings());
                Log.d(TAG, "Got accelerometer data with mean values: " + meanValues[0] + ", " + meanValues[1] + ", " + meanValues[2]);

                srd.setAccelerometerData(new si.uni_lj.fri.taskyapp.data.AccelerometerData(meanValues, ((AccelerometerData) sensingData).getSensorReadings()));
            } else if (sensingData instanceof MicrophoneData) {
                int[] amplitudes = ((MicrophoneData) sensingData).getAmplitudeArray();
                Log.d(TAG, "Mean amplitude from microphone: " + SensorsHelper.getMeanValue(amplitudes));

                srd.setMicrophoneData(new si.uni_lj.fri.taskyapp.data.MicrophoneData(amplitudes));
            } else if (sensingData instanceof WifiData) {
                ArrayList<WifiScanResult> wifiScanResults = ((WifiData) sensingData).getWifiScanData();
                Log.d(TAG, "WiFi data SSIDs nearby:");
                for (WifiScanResult wsr : wifiScanResults) {
                    Log.d(TAG, wsr.getSsid());
                }
                environmentData.setnWifiDevicesNearby(wifiScanResults.size());
            } else {
                Log.d(TAG, "Retrieved unhandled sensing object: " + sensingData);
            }
        }

        unsubscribeFromSensors(sm);

        float lightPercentage = sumLightValues / countLightValues;
        if(Float.isNaN(lightPercentage)){
            lightPercentage = -1.f;
        }
        environmentData.setAverageLightPercentageValue(lightPercentage);
        srd.setEnvironmentData(environmentData);
        srd.setTimestampEnded(System.currentTimeMillis());
        srd.setPhoneStatusData(phoneStatusDataList);


        Log.d(TAG, "Finishing with SenseDataIntentService method.");
        Log.d(TAG, "Result: " + new Gson().toJson(srd));

        // Saving sensor readings to database
        new SensorReadingRecord(srd).save();

    }

    /*
     * Google API client section
     */
    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataSensed(SensorData data) {
        if(data instanceof ScreenData && phoneStatusDataList != null){
            //Log.d(TAG, "Screen is: " + ((ScreenData) data).isOn());
            PhoneStatusData psd = new PhoneStatusData();
            psd.setScreenOn(((ScreenData) data).isOn());
            psd.setMillisAfterStart(data.getTimestamp() - timeSensingStarted);
            phoneStatusDataList.add(psd);
        }
        else if(data instanceof LightData){
            //Log.d(TAG, "LightData, max range: " + ((LightData) data).getValue());
            countLightValues++;
            sumLightValues += (((LightData) data).getValue() / ((LightData) data).getMaxRange());
        }
    }


    private void subscribeToSensors(ESSensorManager sensorManager) {
        for(int sensorType : SENSOR_IDS){
            try {
                sensorManager.subscribeToSensorData(sensorType, this);
            } catch (ESException e) {
                Log.e(TAG, "Error connecting to server type, int = " + sensorType + ", message = " + e.getMessage());
            }
        }
    }

    private void unsubscribeFromSensors(ESSensorManager sensorManager){
        for(int subscriptionId : sensorSubscriptionIds){
            try {
                sensorManager.unsubscribeFromSensorData(subscriptionId);
            } catch (ESException e) {
                Log.e(TAG, "Error unsubscribing from sensor, int = " + subscriptionId + ", message = " + e.getMessage());
            }
        }
    }
    @Override
    public void onCrossingLowBatteryThreshold(boolean isBelowThreshold) {

    }
}
