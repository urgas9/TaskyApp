package si.uni_lj.fri.taskyapp.data;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class AccelerometerData {
    private float meanX;
    private float meanY;
    private float meanZ;


    public AccelerometerData(float[] meanValues){
        super();
        this.meanX = meanValues[0];
        this.meanY = meanValues[1];
        this.meanZ = meanValues[2];
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

    @Override
    public String toString() {
        return "AccelerometerData{" +
                "meanX=" + meanX +
                ", meanY=" + meanY +
                ", meanZ=" + meanZ +
                '}';
    }
}
