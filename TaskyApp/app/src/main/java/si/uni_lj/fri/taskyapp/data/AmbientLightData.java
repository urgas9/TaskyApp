package si.uni_lj.fri.taskyapp.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by urgas9 on 27.3.16, OpenHours.com
 */
public class AmbientLightData {

    private float min;
    private float max;
    private float mean;
    @SerializedName("max_range")
    private float maxRange;

    public AmbientLightData(float min, float max, float mean, float maxRange) {
        super();
        this.min = min;
        this.max = max;
        this.mean = mean;
        this.maxRange = maxRange;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getMean() {
        return mean;
    }

    public void setMean(float mean) {
        this.mean = mean;
    }

    public float getMaxPossible() {
        return maxRange;
    }

    public void setMaxPossible(float maxRange) {
        this.maxRange = maxRange;
    }

    @Override
    public String toString() {
        return "AmbientLightData{" +
                "min=" + min +
                ", max=" + max +
                ", mean=" + mean +
                ", maxPossible=" + maxRange +
                '}';
    }
}
