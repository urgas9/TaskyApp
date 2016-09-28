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
