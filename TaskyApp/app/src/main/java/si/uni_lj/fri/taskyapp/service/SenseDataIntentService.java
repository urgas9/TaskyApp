package si.uni_lj.fri.taskyapp.service;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
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
import com.ubhave.sensormanager.data.push.ConnectionStrengthData;
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
    private static final int[] SENSOR_IDS = {SensorUtils.SENSOR_TYPE_LIGHT,
            SensorUtils.SENSOR_TYPE_SCREEN,
            SensorUtils.SENSOR_TYPE_CONNECTION_STRENGTH};
    private GoogleApiClient mGoogleApiClient;
    private List<Integer> sensorSubscriptionIds;

    //Data for callbacks
    private long mTimeSensingStarted;
    private List<PhoneStatusData> mPhoneStatusDataList;
    private float mSumLightValues = 0;
    private long mCountLightValues = 0;
    private SensingDecisionHelper mSensingHelper;

    private float mSumConnectionStrengthValues;
    private long mCountConnectionStrengthValues;

    private List<ActivityData> mDetectedActivityList;
    private PendingIntent mActivityRecognitionIntent;


    public SenseDataIntentService() {
        super("SenseDataIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String policy = intent.getStringExtra("sensing_policy");
        int userLabel = intent.getIntExtra("user_label", -2);
        if (userLabel > 0) {
            Log.d(TAG, "+++++++++++++++ Force sensing set to true!");
        }
        mSensingHelper = new SensingDecisionHelper(getApplicationContext(), userLabel);

        mSumConnectionStrengthValues = 0;
        mSumLightValues = 0;
        mCountConnectionStrengthValues = 0;
        mCountLightValues = 0;

        if (!mSensingHelper.decideOnMinimumIntervalTimeDifference()) {
            Log.d(TAG, "decideOnMinimumIntervalTimeDifference decided not to sense");
            return;
        }
        // Blocking connect to Google API, so we will have a connected instance in future
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            Log.d(TAG, "Connecting to GoogleApiClient");
            buildGoogleApiClient().blockingConnect(10, TimeUnit.SECONDS);
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
        //detectedActivity = new DetectedActivity(DetectedActivity.STILL, 90);
        if (detectedActivity == null) {
            mDetectedActivityList = new LinkedList<>();
            if (mGoogleApiClient.isConnected()) {
                Log.d(TAG, "Start Activity Recognition");
                Status status = ActivityRecognition.
                        ActivityRecognitionApi.
                        requestActivityUpdates(mGoogleApiClient, 1000, getPendingIntentForActivityRecognition())
                        .await();
                if (status.isSuccess()) {
                    Log.d(TAG, "Successfully requested Activity Recognition updates.");
                } else {
                    Log.e(TAG, "Cannot start Activity Recognition updates. \n Message: " + status.getStatusMessage());
                }
            } else {
                Log.d(TAG, "Cannot start activity recognition, GoogleAPIClient not connected.");
            }
        }
        srd.setActivityData(new ActivityData(detectedActivity));
        srd.setLocationData(new LocationData(getApplicationContext(), sensedLocation));

        mTimeSensingStarted = System.currentTimeMillis();
        srd.setTimestampStarted(mTimeSensingStarted);

        if (!mSensingHelper.shouldContinueSensing(srd)) {
            Log.d(TAG, "Decided not to sense this time!");
            return;
        }
        Log.d(TAG, "Decided to start sensing.");
        mSensingHelper.saveNewDecisiveSensingData(srd);

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
        mPhoneStatusDataList = new ArrayList<>();
        Log.d(TAG, "Subscribed to sensors.");
        Log.d(TAG, "Now, trying to get sensor results.");

        Future futureSensedData;
        EnvironmentData environmentData = new EnvironmentData();
        environmentData.setWifiTurnedOn(SensorsHelper.isWifiEnabled(getBaseContext()));
        environmentData.setBluetoothTurnedOn(SensorsHelper.isBluetoothEnabled());

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

        float lightPercentage = mSumLightValues / mCountLightValues;
        if (Float.isNaN(lightPercentage)) {
            lightPercentage = -1.f;
        }
        environmentData.setAverageLightPercentageValue(lightPercentage);

        float connectionStrengthPercentage = mSumConnectionStrengthValues / mCountConnectionStrengthValues;
        if (Float.isNaN(connectionStrengthPercentage)) {
            connectionStrengthPercentage = -1.f;
        }
        srd.setEnvironmentData(environmentData);
        srd.setTimestampEnded(System.currentTimeMillis());
        srd.setPhoneStatusData(mPhoneStatusDataList);

        srd.setActivityData(extractMostProbableActivity());
        if (srd.getActivityData() == null && detectedActivity != null) {
            srd.setActivityData(new ActivityData(detectedActivity));
        }

        if (sensedLocation != null) {
            srd.setLocationData(new LocationData(getApplicationContext(), sensedLocation));
        }
        if(userLabel > 0){
            srd.setLabel(userLabel);
        }
        Log.d(TAG, "Finishing with SenseDataIntentService method.");
        Log.d(TAG, "Result: " + new Gson().toJson(srd));
        // Persisting sensor readings to database
        new SensorReadingRecord(srd, userLabel > 0, userLabel).save();

        // Shutting down everything
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, getPendingIntentForActivityRecognition());
            mGoogleApiClient.disconnect();
        }
    }

    private ActivityData extractMostProbableActivity() {
        ActivityData mostProbableActivity = null;
        boolean inDoubt = false;
        if(mDetectedActivityList == null){
            return null;
        }
        for (ActivityData ad : mDetectedActivityList) {
            if (mostProbableActivity == null || mostProbableActivity.getActivityType().equals(ad) && mostProbableActivity.getConfidence() > ad.getConfidence()) {
                mostProbableActivity = ad;
            } else if (mostProbableActivity.equals(ad) && ad.getConfidence() > mostProbableActivity.getConfidence()) {
                mostProbableActivity = ad;
                inDoubt = false;
            } else if (!inDoubt && ad.getConfidence() > 90 && !ad.getActivityType().equals(mostProbableActivity.getActivityType())) {
                inDoubt = true;
            } else if (inDoubt && ad.getConfidence() > 90 && !ad.getActivityType().equals(mostProbableActivity.getActivityType())) {
                mostProbableActivity = ad;
                inDoubt = false;
            }
        }
        return mostProbableActivity;
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
        if (data instanceof ScreenData && mPhoneStatusDataList != null) {
            //Log.d(TAG, "Screen is: " + ((ScreenData) data).isOn());
            PhoneStatusData psd = new PhoneStatusData();
            psd.setScreenOn(((ScreenData) data).isOn());
            psd.setMillisAfterStart(data.getTimestamp() - mTimeSensingStarted);
            mPhoneStatusDataList.add(psd);
        } else if (data instanceof LightData) {
            //Log.d(TAG, "LightData, max range: " + ((LightData) data).getValue());
            mCountLightValues++;
            mSumLightValues += (((LightData) data).getValue() / ((LightData) data).getMaxRange());
        }
        else if(data instanceof ConnectionStrengthData){
            mCountConnectionStrengthValues++;
            mSumConnectionStrengthValues += ((ConnectionStrengthData) data).getStrength();
        }
    }

    private GoogleApiClient buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext())
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

    private PendingIntent getPendingIntentForActivityRecognition() {
        Intent broadcastIntent = new Intent(getBaseContext(), MyActivityRecognitionIntentService.class);
        return PendingIntent.getService(getBaseContext(), 11, broadcastIntent, PendingIntent.FLAG_ONE_SHOT);
    }

    private void subscribeToSensors(ESSensorManager sensorManager) {
        for (int sensorType : SENSOR_IDS) {
            try {
                sensorManager.subscribeToSensorData(sensorType, this);
            } catch (ESException e) {
                Log.e(TAG, "Error connecting to server type, int = " + sensorType + ", message = " + e.getMessage());
            }
        }
    }

    private void unsubscribeFromSensors(ESSensorManager sensorManager) {
        for (int subscriptionId : sensorSubscriptionIds) {
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

    class MyActivityRecognitionIntentService extends IntentService {

        /**
         * Creates an IntentService.  Invoked by your subclass's constructor.
         *
         * @param name Used to name the worker thread, important only for debugging.
         */
        public MyActivityRecognitionIntentService(String name) {
            super(name);
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Log.d(TAG, "onHandleIntent in MyActivityRecognitionIntentService");
            if (ActivityRecognitionResult.hasResult(intent)) {
                mDetectedActivityList.add(new ActivityData(ActivityRecognitionResult.extractResult(intent).getMostProbableActivity()));
            }
        }
    }

}
