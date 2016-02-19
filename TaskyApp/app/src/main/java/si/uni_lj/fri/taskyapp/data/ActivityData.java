package si.uni_lj.fri.taskyapp.data;

import com.google.android.gms.location.DetectedActivity;
import com.google.gson.annotations.SerializedName;

import si.uni_lj.fri.taskyapp.global.SensorsHelper;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class ActivityData {

    @SerializedName("type")
    private String activityType;
    private int confidence;

    public ActivityData(DetectedActivity detectedActivity) {
        super();
        if (detectedActivity != null) {
            this.activityType = SensorsHelper.getDetectedActivityName(detectedActivity.getType());
            this.confidence = detectedActivity.getConfidence();
        }

    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ActivityData && ((ActivityData) o).getActivityType().equals(this.getActivityType())) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "ActivityData{" +
                "activityType='" + activityType + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
