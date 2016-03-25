package si.uni_lj.fri.taskyapp.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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
    @SerializedName("bluetooth_mac_addresses")
    private List<String> bluetoothMacAddresses;

    @SerializedName("battery_charging")
    private Boolean isBatteryCharging;

    @SerializedName("average_light")
    private float averageLightPercentageValue;

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

    public Boolean isBatteryCharging() {
        return isBatteryCharging;
    }

    public void setIsBatteryCharging(Boolean isBatteryCharging) {
        this.isBatteryCharging = isBatteryCharging;
    }

    public List<String> getBluetoothMacAddresses() {
        return bluetoothMacAddresses;
    }

    public void setBluetoothMacAddresses(List<String> bluetoothMacAddresses) {
        this.bluetoothMacAddresses = bluetoothMacAddresses;
    }

    @Override
    public String toString() {
        return "EnvironmentData{" +
                "nWifiDevicesNearby=" + nWifiDevicesNearby +
                ", wifiTurnedOn=" + wifiTurnedOn +
                ", nBluetoothDevicesNearby=" + nBluetoothDevicesNearby +
                ", bluetoothTurnedOn=" + bluetoothTurnedOn +
                ", isBatteryCharging=" + isBatteryCharging +
                ", averageLightPercentageValue=" + averageLightPercentageValue +
                '}';
    }
}
