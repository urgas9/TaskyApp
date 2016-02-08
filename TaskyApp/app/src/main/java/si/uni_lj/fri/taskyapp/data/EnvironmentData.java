package si.uni_lj.fri.taskyapp.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class EnvironmentData {

    @SerializedName("num_wifi_devices_nearby")
    private int nWifiDevicesNearby;
    @SerializedName("wifi_turned_on")
    private boolean wifiTurnedOn;
    @SerializedName("num_bluetooth_devices_nearby")
    private int nBluetoothDevicesNearby;
    @SerializedName("bluetooth_turned_on")
    private boolean bluetoothTurnedOn;

    @SerializedName("average_light")
    private float averageLightPercentageValue;
    @SerializedName("average_connection_strength")
    private float averageConnectionStrengthPercentageValue;

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

    public float getAverageLightPercentageValue() {
        return averageLightPercentageValue;
    }

    public void setAverageLightPercentageValue(float averageLightPercentageValue) {
        this.averageLightPercentageValue = averageLightPercentageValue;
    }

    public boolean isWifiTurnedOn() {
        return wifiTurnedOn;
    }

    public void setWifiTurnedOn(boolean wifiTurnedOn) {
        this.wifiTurnedOn = wifiTurnedOn;
    }

    public boolean isBluetoothTurnedOn() {
        return bluetoothTurnedOn;
    }

    public void setBluetoothTurnedOn(boolean bluetoothTurnedOn) {
        this.bluetoothTurnedOn = bluetoothTurnedOn;
    }

    public float getAverageConnectionStrengthPercentageValue() {
        return averageConnectionStrengthPercentageValue;
    }

    public void setAverageConnectionStrengthPercentageValue(float averageConnectionStrengthPercentageValue) {
        this.averageConnectionStrengthPercentageValue = averageConnectionStrengthPercentageValue;
    }

    @Override
    public String toString() {
        return "EnvironmentData{" +
                "nWifiDevicesNearby=" + nWifiDevicesNearby +
                ", wifiTurnedOn=" + wifiTurnedOn +
                ", nBluetoothDevicesNearby=" + nBluetoothDevicesNearby +
                ", bluetoothTurnedOn=" + bluetoothTurnedOn +
                ", averageLightPercentageValue=" + averageLightPercentageValue +
                ", averageConnectionStrengthPercentageValue=" + averageConnectionStrengthPercentageValue +
                '}';
    }
}
