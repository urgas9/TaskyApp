package si.uni_lj.fri.taskyapp.data;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class SensorReadingData {

    @SerializedName("app_version")
    private int appVersion;

    @SerializedName("sensing_policy")
    private String sensingPolicy;

    @SerializedName("t_started")
    private long timestampStarted;

    @SerializedName("t_ended")
    private long timestampEnded;

    @SerializedName("location")
    private LocationData locationData;

    @SerializedName("activity")
    private ActivityData activityData;

    @SerializedName("environment")
    private EnvironmentData environmentData;

    @SerializedName("microphone")
    private MicrophoneData microphoneData;

    @SerializedName("accelerometer")
    private AccelerometerData accelerometerData;

    @SerializedName("phone_status_list")
    private List<PhoneStatusData> phoneStatusData;

    public SensorReadingData(Context context){
        try {
            this.appVersion = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SensorReadingData", "Cannot get version code: " + e.getMessage());
        }
    }

    public int getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(int appVersion) {
        this.appVersion = appVersion;
    }

    public long getTimestampStarted() {
        return timestampStarted;
    }

    public void setTimestampStarted(long timestampStarted) {
        this.timestampStarted = timestampStarted;
    }

    public long getTimestampEnded() {
        return timestampEnded;
    }

    public void setTimestampEnded(long timestampEnded) {
        this.timestampEnded = timestampEnded;
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

    public AccelerometerData getAccelerometerData() {
        return accelerometerData;
    }

    public void setAccelerometerData(AccelerometerData accelerometerData) {
        this.accelerometerData = accelerometerData;
    }

    public List<PhoneStatusData> getPhoneStatusData() {
        return phoneStatusData;
    }

    public void setPhoneStatusData(List<PhoneStatusData> phoneStatusData) {
        this.phoneStatusData = phoneStatusData;
    }

    public LocationData getLocationData() {
        return locationData;
    }

    public void setLocationData(LocationData locationData) {
        this.locationData = locationData;
    }

    @Override
    public String toString() {
        return "SensorReadingData{" +
                "appVersion=" + appVersion +
                ", sensingPolicy='" + sensingPolicy + '\'' +
                ", timestampStarted=" + timestampStarted +
                ", timestampEnded=" + timestampEnded +
                ", locationData=" + locationData +
                ", activityData=" + activityData +
                ", environmentData=" + environmentData +
                ", microphoneData=" + microphoneData +
                ", accelerometerData=" + accelerometerData +
                ", phoneStatusData=" + phoneStatusData +
                '}';
    }
}

