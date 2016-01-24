package si.uni_lj.fri.taskyapp.global;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class SensorsHelper {

    //Get the activity name
    public static String getDetectedActivityName(int type) {
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

    public static float[] getMeanAccelerometerValues(ArrayList<float[]> readings) {
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

    public static int[] getMinAndMaxValues(int[] array){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for(int i  : array){
            min = (i<min)?i:min;
            max = (i>max)?i:max;
        }
        int[] result = {min, max};
        return result;
    }

    public static double getMeanValue(int[] array){
        int size = array.length;
        double meanValue = 0;
        for(int val : array){
            meanValue += val/size;
        }
        return meanValue;
    }


}
