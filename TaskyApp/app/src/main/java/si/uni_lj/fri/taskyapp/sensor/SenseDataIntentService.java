package si.uni_lj.fri.taskyapp.sensor;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationResult;
import com.ubhave.sensormanager.ESException;
import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.config.pull.PullSensorConfig;
import com.ubhave.sensormanager.data.pull.AccelerometerData;
import com.ubhave.sensormanager.data.pull.BluetoothData;
import com.ubhave.sensormanager.data.pull.ESBluetoothDevice;
import com.ubhave.sensormanager.sensors.SensorUtils;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by urgas9 on 31. 12. 2015.
 */
public class SenseDataIntentService extends IntentService {
    //LogCat
    private static final String TAG = SenseDataIntentService.class.getSimpleName();

    public SenseDataIntentService() {
        super("SenseDataIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String policy = intent.getStringExtra("sensing_policy");
        if (ActivityRecognitionResult.hasResult(intent)) {
            //Extract the result from the Response
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity detectedActivity = result.getMostProbableActivity();

            //Get the Confidence and Name of Activity
            int confidence = detectedActivity.getConfidence();
            String mostProbableName = getActivityName(detectedActivity.getType());

            //Fire the intent with activity name & confidence
            Intent i = new Intent("NewSensorReading");
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
            Location loc = result.getLastLocation();
            Intent i = new Intent("NewSensorReading");
            i.putExtra("policy", "location");
            i.putExtra("location", loc);

            this.sendBroadcast(i);

        } else if (policy != null && policy.equals("INTERVAL")) {
            Log.d(TAG, "Got intent from fired alarm.");

            Intent i = new Intent("NewSensorReading");
            i.putExtra("policy", "alarm");
        } else {
            Log.d(TAG, "Policy unresolved: " + policy);
        }

        final ESSensorManager sm;
        try {
            sm = ESSensorManager.getSensorManager(getApplicationContext());
            sm.setSensorConfig(SensorUtils.SENSOR_TYPE_ACCELEROMETER,
                    PullSensorConfig.SENSE_WINDOW_LENGTH_MILLIS, Constants.SENSING_WINDOW_LENGTH_MILLIS);
        } catch (ESException e) {
            e.printStackTrace();
            Log.e(TAG, "Cannot start sensing due to an exception, message: " + e.getMessage());
            return;
        }

        SensorThreadsManager sensorThreadsManager = new SensorThreadsManager();

        sensorThreadsManager.submit(new Callable() {
            @Override
            public Object call() throws Exception {
                Thread.sleep(1200);
                return "Ajga Bidona!";
            }
        });
        sensorThreadsManager.submit(new Callable<AccelerometerData>() {
            @Override
            public AccelerometerData call() throws Exception {
                return (AccelerometerData) sm.getDataFromSensor(SensorUtils.SENSOR_TYPE_ACCELEROMETER);
            }
        });
        sensorThreadsManager.submit(new Callable<BluetoothData>() {
            @Override
            public BluetoothData call() throws Exception {
                return (BluetoothData) sm.getDataFromSensor(SensorUtils.SENSOR_TYPE_BLUETOOTH);
            }
        });

        sensorThreadsManager.submit(new Callable() {
            @Override
            public String call() throws Exception {
                return "dummy";
            }
        });

        Log.d(TAG, "Threads submitted, trying to get results.");
        Future futureSensedData;
        while (sensorThreadsManager.moreResultsAvailable()) {
            Object sensingData = null;
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
            } else if (sensingData instanceof AccelerometerData) {
                float[] meanValues = getMeanAccelerometerValues(((AccelerometerData) sensingData).getSensorReadings());
                Log.d(TAG, "Got accelerometer data with mean values: " + meanValues[0] + ", " + meanValues[1] + ", " + meanValues[2]);
            } else {
                Log.d(TAG, "Retrieved unhandled sensing object: " + sensingData);
            }
        }

        Log.d(TAG, "Finishing with method.");


    }

    private float[] getMeanAccelerometerValues(ArrayList<float[]> readings) {
        int size = readings.size();
        float[] result = new float[3];
        for (float[] axes : readings) {
            int i = 0;
            for (float f : axes) {
                result[i] += f / size;
                i++;
            }
        }
        return result;
    }

    //Get the activity name
    private String getActivityName(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.WALKING:
                return "Walking";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.UNKNOWN:
                return "Unknown";
        }
        return "N/A";
    }
}
