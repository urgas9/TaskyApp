package si.uni_lj.fri.taskyapp.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class PhoneStatusData {

    @SerializedName("screen_on")
    private boolean screenOn;

    public boolean isScreenOn() {
        return screenOn;
    }

    public void setScreenOn(boolean screenOn) {
        this.screenOn = screenOn;
    }

    @Override
    public String toString() {
        return "PhoneStatusData{" +
                "screenOn=" + screenOn +
                '}';
    }
}
