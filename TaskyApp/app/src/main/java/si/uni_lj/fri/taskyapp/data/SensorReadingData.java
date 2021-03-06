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

package si.uni_lj.fri.taskyapp.data;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 24. 01. 2016.
 * <p/>
 * Class used to store sensing data
 */
public class SensorReadingData {

    @SerializedName("app_version")
    private int appVersion;

    @SerializedName("sensing_policy")
    private String sensingPolicy;

    @SerializedName("t_started")
    private String timestampStarted;

    @SerializedName("t_started_pretty")
    private String timestampStartedPretty;

    @SerializedName("t_ended")
    private String timestampEnded;

    @SerializedName("location")
    private LocationData locationData;

    @SerializedName("activity")
    private ActivityData activityData;

    @SerializedName("environment")
    private EnvironmentData environmentData;

    @SerializedName("microphone")
    private MicrophoneData microphoneData;

    @SerializedName("accelerometer")
    private MotionSensorData accelerometerData;

    @SerializedName("gyroscope")
    private MotionSensorData gyroscopeData;

    @SerializedName("screen_status_list")
    private List<ScreenStatusData> screenStatusData;

    @SerializedName("active_calendar_events")
    private List<CalendarData> calendarEvents;

    @SerializedName("volume_settings")
    private VolumeSettingsData volumeSettingsData;

    @SerializedName("angel_sensor")
    private AngelSensorData angelSensorData;

    private Integer label; // Label on Likert scale 1-5 (easy - hard)
    @SerializedName("labeled_after_notif_seconds")
    private Integer labeledAfterNotifSeconds;
    @SerializedName("database_id")
    private Long dbRecordId;

    public SensorReadingData(Context context) {
        try {
            this.appVersion = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SensorReadingData", "Cannot get version code: " + e.getMessage());
        }
    }

    public long getTimestampStarted() {
        return Long.valueOf(timestampStarted == null ? "0" : timestampStarted);
    }

    public void setTimestampStarted(long timestampStarted) {
        this.timestampStarted = Long.toString(timestampStarted);
        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT_TO_SHOW_FULL, Locale.ENGLISH);
        this.timestampStartedPretty = format.format(new Date(timestampStarted));
    }

    public long getTimestampEnded() {
        return Long.valueOf(timestampEnded);
    }

    public void setTimestampEnded(long timestampEnded) {
        this.timestampEnded = Long.toString(timestampEnded);
    }

    public ActivityData getActivityData() {
        return activityData;
    }

    public void setActivityData(ActivityData activityData) {
        this.activityData = activityData;
    }

    public EnvironmentData getEnvironmentData() {
        return environmentData;
    }

    public void setEnvironmentData(EnvironmentData environmentData) {
        this.environmentData = environmentData;
    }

    public MicrophoneData getMicrophoneData() {
        return microphoneData;
    }

    public void setMicrophoneData(MicrophoneData microphoneData) {
        this.microphoneData = microphoneData;
    }

    public String getSensingPolicy() {
        return sensingPolicy;
    }

    public void setSensingPolicy(String sensingPolicy) {
        this.sensingPolicy = sensingPolicy;
    }

    public MotionSensorData getAccelerometerData() {
        return accelerometerData;
    }

    public void setAccelerometerData(MotionSensorData accelerometerData) {
        this.accelerometerData = accelerometerData;
    }

    public List<ScreenStatusData> getScreenStatusData() {
        return screenStatusData;
    }

    public void setScreenStatusData(List<ScreenStatusData> screenStatusData) {
        this.screenStatusData = screenStatusData;
    }

    public LocationData getLocationData() {
        return locationData;
    }

    public void setLocationData(LocationData locationData) {
        this.locationData = locationData;
    }

    public Integer getLabel() {
        return label;
    }

    public void setLabel(Integer label) {
        this.label = label;
    }

    public boolean equalByDayTimestampStarted(SensorReadingData srd) {
        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT_TO_SHOW_DAY, Locale.ENGLISH);
        if (timestampStarted != null) {
            long tStarted = Long.valueOf(timestampStarted);
            String dayThis = format.format(new Date(tStarted));
            String daySrd = format.format(new Date(tStarted));
            return dayThis.equals(daySrd);
        }
        return false;
    }

    public Long getDbRecordId() {
        return dbRecordId;
    }

    public void setDbRecordId(Long dbRecordId) {
        this.dbRecordId = dbRecordId;
    }

    public MotionSensorData getGyroscopeData() {
        return gyroscopeData;
    }

    public void setGyroscopeData(MotionSensorData gyroscopeData) {
        this.gyroscopeData = gyroscopeData;
    }

    public VolumeSettingsData getVolumeSettingsData() {
        return volumeSettingsData;
    }

    public void setVolumeSettingsData(VolumeSettingsData volumeSettingsData) {
        this.volumeSettingsData = volumeSettingsData;
    }

    public List<CalendarData> getCalendarEvents() {
        return calendarEvents;
    }

    public void setCalendarEvents(List<CalendarData> calendarEvents) {
        this.calendarEvents = calendarEvents;
    }

    public Integer getLabeledAfterNotifSeconds() {
        return labeledAfterNotifSeconds;
    }

    public void setLabeledAfterNotifSeconds(Integer labeledAfterNotifSeconds) {
        this.labeledAfterNotifSeconds = labeledAfterNotifSeconds;
    }

    public AngelSensorData getAngelSensorData() {
        return angelSensorData;
    }

    public void setAngelSensorData(AngelSensorData angelSensorData) {
        this.angelSensorData = angelSensorData;
    }

    @Override
    public String toString() {
        return "SensorReadingData{" +
                "appVersion=" + appVersion +
                ", sensingPolicy='" + sensingPolicy + '\'' +
                ", timestampStarted='" + timestampStarted + '\'' +
                ", timestampStartedPretty='" + timestampStartedPretty + '\'' +
                ", timestampEnded='" + timestampEnded + '\'' +
                ", locationData=" + locationData +
                ", activityData=" + activityData +
                ", environmentData=" + environmentData +
                ", microphoneData=" + microphoneData +
                ", accelerometerData=" + accelerometerData +
                ", gyroscopeData=" + gyroscopeData +
                ", screenStatusData=" + screenStatusData +
                ", calendarEvents=" + calendarEvents +
                ", volumeSettingsData=" + volumeSettingsData +
                ", angelSensorData=" + angelSensorData +
                ", labeledAfterNotifSeconds=" + labeledAfterNotifSeconds +
                ", label=" + label +
                ", dbRecordId=" + dbRecordId +
                '}';
    }
}

