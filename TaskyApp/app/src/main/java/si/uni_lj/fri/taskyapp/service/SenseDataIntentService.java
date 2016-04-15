package si.uni_lj.fri.taskyapp.service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
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
import com.ubhave.sensormanager.data.pull.GyroscopeData;
import com.ubhave.sensormanager.data.pull.MicrophoneData;
import com.ubhave.sensormanager.data.pull.WifiData;
import com.ubhave.sensormanager.data.pull.WifiScanResult;
import com.ubhave.sensormanager.data.push.ScreenData;
import com.ubhave.sensormanager.sensors.SensorUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import si.uni_lj.fri.taskyapp.data.ActivityData;
import si.uni_lj.fri.taskyapp.data.AmbientLightData;
import si.uni_lj.fri.taskyapp.data.EnvironmentData;
import si.uni_lj.fri.taskyapp.data.LocationData;
import si.uni_lj.fri.taskyapp.data.MotionSensorData;
import si.uni_lj.fri.taskyapp.data.OfficeHoursObject;
import si.uni_lj.fri.taskyapp.data.ScreenStatusData;
import si.uni_lj.fri.taskyapp.data.SensorReadingData;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.global.SensingDecisionHelper;
import si.uni_lj.fri.taskyapp.global.SensingPolicy;
import si.uni_lj.fri.taskyapp.global.SensorsHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;
import si.uni_lj.fri.taskyapp.sensor.SensingInitiator;
import si.uni_lj.fri.taskyapp.sensor.SensorCallableGenerator;
import si.uni_lj.fri.taskyapp.sensor.SensorThreadsManager;

/**
 * Created by urgas9 on 31. 12. 2015.
 */
public class SenseDataIntentService extends IntentService implements GoogleApiClient.ConnectionCallbacks, SensorDataListener {
    //LogCat
    private static final String TAG = SenseDataIntentService.class.getSimpleName();
    private static final int[] SENSOR_IDS = {
            SensorUtils.SENSOR_TYPE_LIGHT,
            SensorUtils.SENSOR_TYPE_SCREEN};
    private static List<ActivityData> mDetectedActivityList;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mActivityRecognitionPendingIntent;
    private List<Integer> sensorSubscriptionIds;
    //Data for callbacks
    private long mTimeSensingStarted;
    private List<ScreenStatusData> mScreenStatusDataList;
    private double mSumLightValues;
    private float mMinLightValue;
    private float mMaxLightValue;
    private float mMaxRangeLight;
    private long mCountLightValues;
    private SharedPreferences mDefaultPrefs;


    public SenseDataIntentService() {
        super("SenseDataIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            Log.d(TAG, "Received intent is null, quit.");
            return;
        }

        mDefaultPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        showNotification(-1);

        String policy = intent.getStringExtra("sensing_policy");
        SensingPolicy sensingPolicy = SensingPolicy.NONE;

        int userLabel = intent.getIntExtra("user_label", -2);
        if (userLabel > 0) {
            Log.d(TAG, "+++ Force sensing set to true!");
            sensingPolicy = SensingPolicy.USER_FORCED;
        }
        if (!SensingInitiator.isUserParticipating(getBaseContext(), userLabel > 0)) {
            Log.d(TAG, "Service: User is not participating, quit.");
            return;
        }

        OfficeHoursObject oho = new OfficeHoursObject(getBaseContext());
        oho.showReminderPrizeNotification(getBaseContext());

        SensingDecisionHelper mSensingHelper = new SensingDecisionHelper(getApplicationContext(), userLabel);

        mSumLightValues = 0;
        mCountLightValues = 0;
        mMinLightValue = Float.MAX_VALUE;
        mMaxLightValue = Float.MIN_VALUE;
        mCountLightValues = 0;
        mMaxRangeLight = -1;

        if (!mSensingHelper.decideOnOfficeHours()) {
            int hourNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            Log.d(TAG, "decideOnOfficeHours decided not to sense, hour = " + hourNow);
            return;
        }
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

        if (sensingPolicy != SensingPolicy.USER_FORCED) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                //Extract the result from the Response
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                detectedActivity = result.getMostProbableActivity();

                //Get the Confidence and Name of Activity
                int confidence = detectedActivity.getConfidence();
                String mostProbableName = SensorsHelper.getDetectedActivityName(detectedActivity.getType());


                Log.d(TAG, "Got intent from activity recognition API update. [" + mostProbableName + ", " + confidence + "]");
                sensingPolicy = SensingPolicy.ACTIVITY_UPDATES;

            } else if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {
                Log.d(TAG, "Got intent from location update.");

                sensedLocation = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
                sensingPolicy = SensingPolicy.LOCATION_UPDATES;

            } else if (policy != null && policy.equals("INTERVAL")) {
                Log.d(TAG, "Got intent from fired alarm.");

            } else {
                Log.d(TAG, "Policy unresolved: " + policy);
            }
        }

        Log.d(TAG, "Sensing service started with sensing policy: " + sensingPolicy.toString());
        srd.setSensingPolicy(sensingPolicy.toString());

        // We need to get activity and location instance, as one or another may not exist at this point
        if (sensedLocation == null) {
            sensedLocation = getLastLocation();
        }
        //detectedActivity = new DetectedActivity(DetectedActivity.STILL, 90);
        if (detectedActivity == null) {
            mDetectedActivityList = new LinkedList<>();
            mActivityRecognitionPendingIntent = getPendingIntentForActivityRecognition();
            if (mGoogleApiClient.isConnected()) {
                Log.d(TAG, "Start Activity Recognition");
                Status status = ActivityRecognition.
                        ActivityRecognitionApi.
                        requestActivityUpdates(mGoogleApiClient, 500, mActivityRecognitionPendingIntent)
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

        mScreenStatusDataList = new ArrayList<>();
        if (mDefaultPrefs.contains(Constants.PREFS_LAST_SCREEN_STATE)) {
            ScreenStatusData psd = new ScreenStatusData();
            psd.setScreenOn(mDefaultPrefs.getBoolean(Constants.PREFS_LAST_SCREEN_STATE, false));
            psd.setMillisAfterStart(0);
            mScreenStatusDataList.add(psd);
        }

        Log.d(TAG, "Decided to start sensing.");
        mSensingHelper.saveNewDecisiveSensingData(srd);

        final ESSensorManager sm;
        try {
            sm = ESSensorManager.getSensorManager(getApplicationContext());
            sm.setSensorConfig(SensorUtils.SENSOR_TYPE_ACCELEROMETER,
                    PullSensorConfig.SENSE_WINDOW_LENGTH_MILLIS, Constants.SENSING_WINDOW_LENGTH_MILLIS);
            sm.setSensorConfig(SensorUtils.SENSOR_TYPE_MICROPHONE, PullSensorConfig.SENSE_WINDOW_LENGTH_MILLIS, Constants.SENSING_WINDOW_LENGTH_MILLIS);
            sm.setSensorConfig(SensorUtils.SENSOR_TYPE_GYROSCOPE, PullSensorConfig.SENSE_WINDOW_LENGTH_MILLIS, Constants.SENSING_WINDOW_LENGTH_MILLIS);

        } catch (ESException e) {
            e.printStackTrace();
            Log.e(TAG, "Cannot start sensing due to an exception, message: " + e.getMessage());
            return;
        }
        sensorSubscriptionIds = new LinkedList<>();

        SensorThreadsManager sensorThreadsManager = new SensorThreadsManager();

        sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_ACCELEROMETER));
        sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_GYROSCOPE));
        sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_BLUETOOTH));
        sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_MICROPHONE));
        sensorThreadsManager.submit(SensorCallableGenerator.getSensorDataCallable(sm, SensorUtils.SENSOR_TYPE_WIFI));
        Log.d(TAG, "Threads submitted");

        // Subscribing to push sensors
        subscribeToSensors(sm);
        Log.d(TAG, "Subscribed to sensors.");
        Log.d(TAG, "Now, trying to get sensor results.");

        Future futureSensedData;
        EnvironmentData environmentData = new EnvironmentData();
        environmentData.setWifiTurnedOn(SensorsHelper.isWifiEnabled(getBaseContext()));
        environmentData.setBluetoothTurnedOn(SensorsHelper.isBluetoothEnabled());

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getBaseContext().registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            environmentData.setIsBatteryCharging(isCharging);
            Log.d(TAG, "Battery charging status: " + isCharging);
            /*int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;*/
        } else {
            Log.d(TAG, "Cannot get battery charging status!");
        }

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
                LinkedList<String> btMacAddresses = new LinkedList<>();
                for (ESBluetoothDevice esbd : bDevices) {
                    Log.d(TAG, "BT device: " + esbd.getBluetoothDeviceName());
                    btMacAddresses.add(esbd.getBluetoothDeviceAddress());
                }
                environmentData.setBluetoothMacAddresses(btMacAddresses);
                environmentData.setnBluetoothDevicesNearby(bDevices.size());
            } else if (sensingData instanceof AccelerometerData) {
                float[] meanValues = SensorsHelper.getMeanAccelerometerValues(((AccelerometerData) sensingData).getSensorReadings());
                Log.d(TAG, "Got accelerometer data with mean values: " + meanValues[0] + ", " + meanValues[1] + ", " + meanValues[2]);

                srd.setAccelerometerData(new MotionSensorData(meanValues, ((AccelerometerData) sensingData).getSensorReadings()));
            } else if (sensingData instanceof GyroscopeData) {
                float[] meanValues = SensorsHelper.getMeanAccelerometerValues(((GyroscopeData) sensingData).getSensorReadings());
                Log.d(TAG, "Got gyroscope data with mean values: " + meanValues[0] + ", " + meanValues[1] + ", " + meanValues[2]);

                srd.setGyroscopeData(new MotionSensorData(meanValues, ((GyroscopeData) sensingData).getSensorReadings()));
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

        float averageLight = (float) mSumLightValues / mCountLightValues;
        if (!Float.isNaN(averageLight)) {
            environmentData.setAmbientLightData(new AmbientLightData(mMinLightValue, mMaxLightValue, averageLight, mMaxRangeLight));
        }

        srd.setEnvironmentData(environmentData);
        srd.setTimestampEnded(System.currentTimeMillis());
        srd.setScreenStatusData(mScreenStatusDataList);

        srd.setActivityData(extractMostProbableActivity());
        if (srd.getActivityData() == null && detectedActivity != null) {
            srd.setActivityData(new ActivityData(detectedActivity));
        }

        if (sensedLocation != null) {
            srd.setLocationData(new LocationData(getApplicationContext(), sensedLocation));
        }
        if (userLabel > 0) {
            srd.setLabel(userLabel);
        }
        Log.d(TAG, "Finishing with SenseDataIntentService method.");
        Log.d(TAG, "Result: " + new Gson().toJson(srd));
        // Persisting sensor readings to database
        long id = new SensorReadingRecord(getBaseContext(), srd, userLabel > 0, userLabel).save();

        showNotification(id);

        Intent i = new Intent(Constants.ACTION_NEW_SENSOR_READING_RECORD);
        i.putExtra("id", id);
        //Send Broadcast to be listen in MainActivity
        this.sendBroadcast(i);

        sensorThreadsManager.dispose();
        // Shutting down everything
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && mActivityRecognitionPendingIntent != null) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, mActivityRecognitionPendingIntent);
            mGoogleApiClient.disconnect();
        }

    }

    private void showNotification(long id) {
        Calendar calendar = Calendar.getInstance();
        String notificationsFrequency = mDefaultPrefs.getString("notifications_preference", "");
        Long lastTimeNotificationSent = mDefaultPrefs.getLong("last_time_user_notified_to_label", 0);
        long nowMillis = calendar.getTimeInMillis();
        int hoursSinceLastNotification = (int) ((nowMillis - lastTimeNotificationSent) / (AlarmManager.INTERVAL_HOUR));


        switch (notificationsFrequency) {
            case "1":
                if ((calendar.get(Calendar.HOUR_OF_DAY) >= Constants.HOUR_SEND_NOTIFICATION_LATE ||
                        calendar.get(Calendar.HOUR_OF_DAY) >= Constants.HOUR_SEND_NOTIFICATION_EARLY) &&
                        hoursSinceLastNotification >= 7) {
                    AppHelper.showLabelDailyTasksNotification(getBaseContext());
                    mDefaultPrefs.edit().putLong("last_time_user_notified_to_label", nowMillis).apply();
                }
                break;
            case "2":
                if (id > 0) {
                    AppHelper.showLabelDailyTasksNotification(getBaseContext(), id);
                }
                break;
            default:
                if (calendar.get(Calendar.HOUR_OF_DAY) >= Constants.HOUR_SEND_NOTIFICATION_LATE &&
                        hoursSinceLastNotification >= 7) {
                    AppHelper.showLabelDailyTasksNotification(getBaseContext());
                    mDefaultPrefs.edit().putLong("last_time_user_notified_to_label", nowMillis).apply();
                }
                break;
        }

    }

    private ActivityData extractMostProbableActivity() {
        ActivityData mostProbableActivity = null;
        boolean inDoubt = false;

        Log.d(TAG, "extractMostProbableActivity: From list: " + mDetectedActivityList);
        if (mDetectedActivityList == null) {
            return null;
        }
        for (ActivityData ad : mDetectedActivityList) {
            if (mostProbableActivity == null || mostProbableActivity.getActivityType().equals(ad.getActivityType()) && mostProbableActivity.getConfidence() > ad.getConfidence()) {
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
        Log.d(TAG, "extractMostProbableActivity: Decided to go with " + mostProbableActivity);
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
        if (data instanceof ScreenData && mScreenStatusDataList != null) {
            //Log.d(TAG, "Screen is: " + ((ScreenData) data).isOn());
            ScreenStatusData psd = new ScreenStatusData();
            psd.setScreenOn(((ScreenData) data).isOn());
            psd.setMillisAfterStart(data.getTimestamp() - mTimeSensingStarted);
            mScreenStatusDataList.add(psd);
        } else if (data instanceof LightData) {
            //Log.d(TAG, "LightData, max range: " + ((LightData) data).getValue());
            mCountLightValues++;
            mMaxRangeLight = ((LightData) data).getMaxRange();
            float value = ((LightData) data).getValue();
            mSumLightValues += value;
            if (value > mMaxLightValue) {
                mMaxLightValue = value;
            }
            if (value < mMinLightValue) {
                mMinLightValue = value;
            }
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

    public static class MyActivityRecognitionIntentService extends IntentService {

        /**
         * Creates an IntentService.  Invoked by your subclass's constructor.
         *
         * @param name Used to name the worker thread, important only for debugging.
         */
        public MyActivityRecognitionIntentService(String name) {
            super(name);
        }

        public MyActivityRecognitionIntentService() {
            super("MyActivityRecognitionIntentService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Log.d(TAG, "onHandleIntent in MyActivityRecognitionIntentService");
            if (ActivityRecognitionResult.hasResult(intent)) {
                if(mDetectedActivityList == null){
                    mDetectedActivityList = new ArrayList<>();
                }
                Log.d(TAG, "Got activity data: " + ActivityRecognitionResult.extractResult(intent).getMostProbableActivity());
                mDetectedActivityList.add(new ActivityData(ActivityRecognitionResult.extractResult(intent).getMostProbableActivity()));
            }
        }
    }

}
