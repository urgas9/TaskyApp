package si.uni_lj.fri.taskyapp.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class EnvironmentData {

    @SerializedName("num_wifi_devices_nearby")
    private int nWifiDevicesNearby;
    @SerializedName("num_bluetooth_devices_nearby")
    private int nBluetoothDevicesNearby;

    @SerializedName("average_light")
    private float averageLightValue;

    public int getnWifiDevicesNearby() {
        return nWifiDevicesNearby;
    }

    public void setnWifiDevicesNearby(int nWifiDevicesNearby) {
        this.nWifiDevicesNearby = nWifiDevicesNearby;
    }

    public int getnBluetoothDevicesNearby() {
        return nBluetoothDevicesNearby;
    }

    public void setnBluetoothDevicesNearby(int nBluetoothDevicesNearby) {
        this.nBluetoothDevicesNearby = nBluetoothDevicesNearby;
    }

    public float getAverageLightValue() {
        return averageLightValue;
    }

    public void setAverageLightValue(float averageLightValue) {
        this.averageLightValue = averageLightValue;
    }

    @Override
    public String toString() {
        return "EnvironmentData{" +
                "nWifiDevicesNearby=" + nWifiDevicesNearby +
                ", nBluetoothDevicesNearby=" + nBluetoothDevicesNearby +
                ", averageLightValue=" + averageLightValue +
                '}';
    }
}
