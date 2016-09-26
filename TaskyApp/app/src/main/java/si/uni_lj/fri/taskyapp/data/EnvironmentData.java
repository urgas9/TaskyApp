/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This project was developed as part of the paper submitted for the UbitTention workshop paper (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

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

    @SerializedName("ambient_light")
    private AmbientLightData ambientLightData;

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

    public AmbientLightData getAmbientLightData() {
        return ambientLightData;
    }

    public void setAmbientLightData(AmbientLightData ambientLightData) {
        this.ambientLightData = ambientLightData;
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
                ", averageLightPercentageValue=" + ambientLightData +
                '}';
    }
}
