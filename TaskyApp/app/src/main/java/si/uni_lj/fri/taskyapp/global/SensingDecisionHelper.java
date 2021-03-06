/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This project was developed as part of the paper submitted for the UbitTention workshop (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package si.uni_lj.fri.taskyapp.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;

import si.uni_lj.fri.taskyapp.data.ActivityData;
import si.uni_lj.fri.taskyapp.data.LocationData;
import si.uni_lj.fri.taskyapp.data.OfficeHoursObject;
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
    private int userLabel;
    private SensorReadingData mOldSensorData;
    private SensorReadingData mNewSensorData;

    public SensingDecisionHelper(Context context, int userLabel) {
        super();
        this.mContext = context;
        this.mSharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Log.d(TAG, "Object created. Decisions so far: positive = " + getNumDecisionsToSense() + "x, negative = " + getNumDecisionsNotToSense() + "x");
        this.gson = new Gson();
        this.userLabel = userLabel;
    }

    /**
     * Saving newly sensed decisive sensing data (decisive = limited data on which we will decide
     * if we continue sensing additional data)
     *
     * @param srd
     */
    public void saveNewDecisiveSensingData(SensorReadingData srd) {
        mSharedPreferences.edit()
                .putString("previous_sensing_data", gson.toJson(srd))
                .commit();
    }

    public SensorReadingData getPreviousDecisiveSensingData() {
        return gson.fromJson(
                mSharedPreferences.getString("previous_sensing_data", "{}"),
                SensorReadingData.class);
    }

    private void markDecisionToSense(boolean decision) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if (decision) {
            editor.putInt("count_decisions_to_sense", (getNumDecisionsToSense() + 1));
        } else {
            editor.putInt("count_decisions_not_to_sense", (getNumDecisionsNotToSense() + 1));
        }
        editor.apply();
    }

    private int getNumDecisionsToSense() {
        return mSharedPreferences.getInt("count_decisions_to_sense", 0);
    }

    private int getNumDecisionsNotToSense() {
        return mSharedPreferences.getInt("count_decisions_not_to_sense", 0);
    }

    /**
     * Logic to determine if we continue sensing or not to preserve battery energy
     * (we might still be at the same location, or activity is still the same)
     *
     * @param newSensorData
     * @return
     */
    public boolean shouldContinueSensing(SensorReadingData newSensorData) {
        if (userLabel > 0) {
            return true;
        }
        mOldSensorData = getPreviousDecisiveSensingData();
        mNewSensorData = newSensorData;

        if (decideOnTimeDifference()) {
            return true;
        }

        boolean continueSensing = true;
        if (mNewSensorData != null && mOldSensorData != null) {
            if (newSensorData.getActivityData() != null && newSensorData.getLocationData() != null) {
                if (!(continueSensing = decideOnActivityData())) {
                    Log.d(TAG, "activity data return false, now decide on location data");
                    continueSensing = decideOnLocationData();
                } else {
                    Log.d(TAG, "activity data return true (both available)");
                }
            } else if (newSensorData.getActivityData() != null) {
                Log.d(TAG, "decide on activity data");
                continueSensing = decideOnActivityData();
            } else if (newSensorData.getLocationData() != null) {
                Log.d(TAG, "decide on location data");
                continueSensing = decideOnLocationData();
            }
        }

        Log.d(TAG, "continueSensing = " + continueSensing);
        markDecisionToSense(continueSensing);
        return continueSensing;
    }

    public boolean decideOnActivityData() {
        if (mOldSensorData == null || mNewSensorData == null || mOldSensorData.getActivityData() == null || mNewSensorData.getActivityData() == null) {
            return true;
        }

        ActivityData newActivityData = mNewSensorData.getActivityData();
        ActivityData oldActivityData = mOldSensorData.getActivityData();

        Log.d(TAG, "Old Activity data: " + oldActivityData.toString());
        Log.d(TAG, "New Activity data: " + newActivityData.toString());

        if (newActivityData.getConfidence() < 80 || newActivityData.getActivityType().equals("Unknown")) {
            return false;
        }
        if (newActivityData.getActivityType().equals(oldActivityData.getActivityType())) {
            if (isUncertainActivityData(newActivityData)) {
                return decideOnTimeDifference();
            }
            // We have more (or less certain result for more than 50%, so we should sense)
            if (Math.abs(newActivityData.getConfidence() - oldActivityData.getConfidence()) > 50) {
                return true;
            }
            return false;
        }
        return true;
    }

    private boolean isUncertainActivityData(ActivityData activityData) {
        if (activityData == null) {
            return false;
        }
        if (activityData.getConfidence() < 95 || activityData.getActivityType().equals("Unknown")) {
            return true;
        }
        return false;
    }

    private boolean decideOnLocationData() {
        if (mOldSensorData == null || mNewSensorData == null || mOldSensorData.getLocationData() == null || mNewSensorData.getLocationData() == null) {
            return true;
        }
        LocationData newLocData = mNewSensorData.getLocationData();
        LocationData oldLocData = mOldSensorData.getLocationData();

        Log.d(TAG, "Old Location data: " + oldLocData.toString());
        Log.d(TAG, "New Location data: " + newLocData.toString());
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

    public boolean decideOnMinimumIntervalTimeDifference() {
        if (userLabel > 0) {
            return true;
        }
        long prevTimestamp = getPreviousDecisiveSensingData().getTimestampStarted();
        return (System.currentTimeMillis() - prevTimestamp) > Constants.MIN_INTERVAL_MILLIS;
    }

    public boolean decideOnOfficeHours() {
        if (userLabel > 0) {
            return true;
        }
        OfficeHoursObject tre = new OfficeHoursObject(mContext);
        return tre.areNowOfficeHours();
    }

    private boolean decideOnTimeDifference() {
        if (mOldSensorData == null || mNewSensorData == null) {
            return true;
        }
        if ((mNewSensorData.getTimestampStarted() - mOldSensorData.getTimestampStarted()) > Constants.MAX_INTERVAL_WITHOUT_SENSING_DATA_IN_MILLIS) {
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
