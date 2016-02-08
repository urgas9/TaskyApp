package si.uni_lj.fri.taskyapp.global;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.wifi.WifiManager;

import com.google.android.gms.location.DetectedActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    /**
     * Check for Bluetooth.
     * @return True if Bluetooth is available.
     */
    public static boolean isBluetoothEnabled() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
    }

    public static boolean isWifiEnabled(Context ctx){
        WifiManager wifi = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
        return wifi.isWifiEnabled();
    }

    public static String getLocationAddress(Context ctx, double lat, double lng){
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        String locationString = null;
        try {
            List<Address> listAddresses = geocoder.getFromLocation(lat, lng, 1);
            if(null != listAddresses && listAddresses.size()>0){
                locationString = listAddresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locationString;
    }




}
