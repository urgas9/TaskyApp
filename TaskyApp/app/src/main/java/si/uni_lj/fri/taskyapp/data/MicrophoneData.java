package si.uni_lj.fri.taskyapp.data;

import com.google.gson.annotations.SerializedName;

import si.uni_lj.fri.taskyapp.global.SensorsHelper;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class MicrophoneData {
    @SerializedName("max_amplitude")
    private int maxAmplitude;
    @SerializedName("min_amplitude")
    private int minAmplitude;
    @SerializedName("mean_amplitude")
    private double meanAmplitude;

    private int[] amplitudes;

    public MicrophoneData(int[] amplitudes) {
        int[] minMax = SensorsHelper.getMinAndMaxValues(amplitudes);
        this.minAmplitude = minMax[0];
        this.maxAmplitude = minMax[1];
        this.meanAmplitude = SensorsHelper.getMeanValue(amplitudes);
        this.amplitudes = amplitudes;

    }

    public int getMaxAmplitude() {
        return maxAmplitude;
    }

    public void setMaxAmplitude(int maxAmplitude) {
        this.maxAmplitude = maxAmplitude;
    }

    public int getMinAmplitude() {
        return minAmplitude;
    }

    public void setMinAmplitude(int minAmplitude) {
        this.minAmplitude = minAmplitude;
    }

    public double getMeanAmplitude() {
        return meanAmplitude;
    }

    public void setMeanAmplitude(double meanAmplitude) {
        this.meanAmplitude = meanAmplitude;
    }

    @Override
    public String toString() {
        return "MicrophoneData{" +
                "maxAmplitude=" + maxAmplitude +
                ", minAmplitude=" + minAmplitude +
                ", meanAmplitude=" + meanAmplitude +
                '}';
    }
}
