package si.uni_lj.fri.taskyapp.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class ScreenStatusData {

    @SerializedName("screen_on")
    private Boolean screenOn;
    private long millisAfterStart;

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
                ", millisAfterStart=" + millisAfterStart +
                '}';
    }

    public long getMillisAfterStart() {
        return millisAfterStart;
    }

    public void setMillisAfterStart(long millisAfterStart) {
        this.millisAfterStart = millisAfterStart;
    }
}