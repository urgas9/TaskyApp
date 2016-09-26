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

package si.uni_lj.fri.taskyapp.data.db;

import android.content.Context;

import com.google.gson.Gson;
import com.orm.SugarRecord;

import si.uni_lj.fri.taskyapp.data.MarkerDataHolder;
import si.uni_lj.fri.taskyapp.data.SensorReadingData;
import si.uni_lj.fri.taskyapp.global.SensorsHelper;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class SensorReadingRecord extends SugarRecord {

    private boolean startedByUser;
    private Integer label;
    private long timeStartedSensing;
    private String detectedActivity;
    private double locationLat;
    private double locationLng;
    private String address; // Pretty printed lat and lng
    private String sensorJsonObject;
    private Integer labeledAfterNotifSeconds;

    public SensorReadingRecord() {

    }

    public SensorReadingRecord(Context ctx, SensorReadingData sensorReadingData, boolean startedByUser, int label) {
        this.sensorJsonObject = new Gson().toJson(sensorReadingData);
        this.startedByUser = startedByUser;
        this.label = label;
        if (sensorReadingData.getActivityData() != null) {
            this.detectedActivity = sensorReadingData.getActivityData().getActivityType();
        }
        if (sensorReadingData.getLocationData() != null) {
            this.locationLat = sensorReadingData.getLocationData().getLat();
            this.locationLng = sensorReadingData.getLocationData().getLng();
            this.address = SensorsHelper.getLocationAddress(ctx, locationLat, locationLng);
        }
        timeStartedSensing = sensorReadingData.getTimestampStarted();
    }

    public MarkerDataHolder getMarkerDataHolder() {
        return new MarkerDataHolder(getId(), timeStartedSensing, label, locationLat, locationLng);
    }

    public String getSensorJsonObject() {
        return sensorJsonObject;
    }

    public void setSensorJsonObject(String sensorJsonObject) {
        this.sensorJsonObject = sensorJsonObject;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isStartedByUser() {
        return startedByUser;
    }

    public void setStartedByUser(boolean startedByUser) {
        this.startedByUser = startedByUser;
    }

    public Integer getLabel() {
        return label;
    }

    public void setLabel(Integer label) {
        this.label = label;
    }

    public long getTimeStartedSensing() {
        return timeStartedSensing;
    }

    public void setTimeStartedSensing(long timeStartedSensing) {
        this.timeStartedSensing = timeStartedSensing;
    }

    public String getDetectedActivity() {
        return detectedActivity;
    }

    public void setDetectedActivity(String detectedActivity) {
        this.detectedActivity = detectedActivity;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(double locationLng) {
        this.locationLng = locationLng;
    }

    public Integer getLabeledAfterNotifSeconds() {
        return labeledAfterNotifSeconds;
    }

    public void setLabeledAfterNotifSeconds(Integer labeledAfterNotifSeconds) {
        this.labeledAfterNotifSeconds = labeledAfterNotifSeconds;
    }

    @Override
    public String toString() {
        return "SensorReadingRecord{" +
                "startedByUser=" + startedByUser +
                ", label=" + label +
                ", timeStartedSensing=" + timeStartedSensing +
                ", detectedActivity='" + detectedActivity + '\'' +
                ", locationLat=" + locationLat +
                ", locationLng=" + locationLng +
                ", address='" + address + '\'' +
                ", sensorJsonObject='" + sensorJsonObject + '\'' +
                ", labeledAfterNotifSeconds=" + labeledAfterNotifSeconds +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SensorReadingRecord) {
            return ((SensorReadingRecord) o).getId() != null
                    && ((SensorReadingRecord) o).getId().equals(this.getId());
        }
        return super.equals(o);
    }
}
