package si.uni_lj.fri.taskyapp.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;

import si.uni_lj.fri.taskyapp.data.ActivityData;
import si.uni_lj.fri.taskyapp.data.LocationData;
import si.uni_lj.fri.taskyapp.data.SensorReadingData;
import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 26. 01. 2016.
 */
public class SensingDecisionHelper {

    private static final String TAG = "SensingDecisionHelper";
    private static final String PREFS_NAME = "SensingDecisionSharedPrefs";
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private Gson gson;

    public SensingDecisionHelper(Context context){
        super();
        this.mContext = context;
        this.mSharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Log.d(TAG, "Object created. Decisions so far: positive = " + getNumDecisionsToSense() + "x, negative = " + getNumDecisionsNotToSense() + "x");
        this.gson = new Gson();
    }

    /**
     * Saving newly sensed decisive sensing data (decisive = limited data on which we will decide
     * if we continue sensing additional data)
     * @param srd
     */
    public void saveNewDecisiveSensingData(SensorReadingData srd){
        mSharedPreferences.edit()
                .putString("previous_sensing_data", gson.toJson(srd))
                .commit();
    }

    public SensorReadingData getPreviousDecisiveSensingData(){
        return  gson.fromJson(
                mSharedPreferences.getString("previous_sensing_data", "{}"),
                SensorReadingData.class);
    }

    private void markDecisionToSense(boolean decision){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if(decision){
            editor.putInt("count_decisions_to_sense", (getNumDecisionsToSense() + 1));
        }
        else {
            editor.putInt("count_decisions_not_to_sense", (getNumDecisionsNotToSense() + 1));
        }
        editor.commit();
    }

    private int getNumDecisionsToSense(){
        return mSharedPreferences.getInt("count_decisions_to_sense", 0);
    }

    private int getNumDecisionsNotToSense(){
        return mSharedPreferences.getInt("count_decisions_not_to_sense", 0);
    }

    /**
     * Logic to determine if we continue sensing or not to preserve battery energy
     * (we might still be at the same location, or activity is still the same)
     * @param newSensorData
     * @return
     */
    public boolean shouldContinueSensing(SensorReadingData newSensorData){
        SensorReadingData oldSensorData = getPreviousDecisiveSensingData();
        boolean continueSensing = true;
        if(newSensorData != null && oldSensorData != null){

            if(!decideOnTimeDifference(newSensorData.getTimestampStarted(), oldSensorData.getTimestampStarted()))
            {
                continueSensing = decideOnActivityData(newSensorData.getActivityData(),
                        oldSensorData.getActivityData());
            }
        }

        markDecisionToSense(continueSensing);
        return continueSensing;
    }

    private boolean decideOnActivityData(ActivityData newActData, ActivityData oldActData){
        if(newActData == null || oldActData == null){
            return true;
        }
        if(newActData.getActivityType().equals(oldActData.getActivityType())){
            // We have more (or less certain result for more than 50%, so we should sense)
            if(Math.abs(newActData.getConfidence() - oldActData.getConfidence()) > 50){
                return true;
            }
            return false;
        }
        return true;
    }

    private boolean decideOnLocationData(LocationData newLocData, LocationData oldLocData){
        // Do nothing if we are too close to previously sensed location
        if (newLocData.getAccuracy() <= Constants.LOCATION_ACCURACY_AT_LEAST &&
                newLocData.getDistanceTo(oldLocData) < Constants.LOCATION_MIN_DISTANCE_TO_LAST_LOC) {
            Log.d(TAG, "We are still pretty close to previously sensed location. Returning.");

            // Not considering this location, but location is more accurate - save it
            /*if (newLocData.getAccuracy() > 0 && newLocData.getAccuracy() < oldLocData.getAccuracy()) {
                saveNewLocationToSharedPreferences(mPreferences, sensedLocation);
            }*/
            return false;
        }
        return true;
    }

    private boolean decideOnTimeDifference(long newTimestamp, long oldTimestamp){
        if((newTimestamp - oldTimestamp) > Constants.MAX_INTERVAL_WITHOUT_SENSING_DATA_IN_MILLIS){
            return true;
        }
        return false;
    }

    private void saveNewLocationToSharedPreferences(SharedPreferences prefs, Location l) {
        SharedPreferences.Editor editor = prefs.edit();
        AppHelper.putDouble(editor, Constants.PREFS_LAST_LOC_LAT, l.getLatitude());
        AppHelper.putDouble(editor, Constants.PREFS_LAST_LOC_LNG, l.getLongitude());
        editor.putFloat(Constants.PREFS_LAST_LOC_ACCURACY, l.getAccuracy());
        editor.commit();
    }
}
