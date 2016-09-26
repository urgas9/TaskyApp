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
        return o instanceof ActivityData && ((ActivityData) o).getActivityType().equals(this.getActivityType());
    }

    @Override
    public String toString() {
        return "ActivityData{" +
                "activityType='" + activityType + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
