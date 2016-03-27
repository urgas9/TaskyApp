package si.uni_lj.fri.taskyapp.data;

import java.util.ArrayList;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class MotionSensorData {
    private float meanX;
    private float meanY;
    private float meanZ;

    private ArrayList<float[]> values;


    public MotionSensorData(float[] meanValues, ArrayList<float[]> values) {
        super();
        this.meanX = meanValues[0];
        this.meanY = meanValues[1];
        this.meanZ = meanValues[2];
        this.values = values;
    }

    public float getMeanX() {
        return meanX;
    }

    public void setMeanX(float meanX) {
        this.meanX = meanX;
    }

    public float getMeanY() {
        return meanY;
    }

    public void setMeanY(float meanY) {
        this.meanY = meanY;
    }

    public float getMeanZ() {
        return meanZ;
    }

    public void setMeanZ(float meanZ) {
        this.meanZ = meanZ;
    }

    public ArrayList<float[]> getValues() {
        return values;
    }

    public void setValues(ArrayList<float[]> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "AccelerometerData{" +
                "meanX=" + meanX +
                ", meanY=" + meanY +
                ", meanZ=" + meanZ +
                '}';
    }
}
