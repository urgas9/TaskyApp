package si.uni_lj.fri.taskyapp.sensor;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationResult;

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
            i.putExtra("activity", mostProbableName);
            i.putExtra("confidence", confidence);

            Log.d(TAG, "Most Probable Name : " + mostProbableName);
            Log.d(TAG, "Confidence : " + confidence);


            //Send Broadcast to be listen in MainActivity
            this.sendBroadcast(i);

        } else if (LocationResult.hasResult(intent)) {
            Log.d(TAG, "Got intent from location update.");

        } else if (policy != null && policy.equals("INTERVAL")){
            Log.d(TAG, "Got intent from fired alarm.");
        } else{
            Log.d(TAG, "Policy unresolved: " + policy);
        }

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
