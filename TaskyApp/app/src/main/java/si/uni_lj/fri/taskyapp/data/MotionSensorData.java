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
