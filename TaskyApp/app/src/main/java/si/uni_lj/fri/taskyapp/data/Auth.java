package si.uni_lj.fri.taskyapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.annotations.SerializedName;

import si.uni_lj.fri.taskyapp.global.AppHelper;

/**
 * Created by urgas9 on 16. 11. 2015.
 */
public class Auth {

    private String email;
    @SerializedName("device_id")
    private String deviceId;
    @SerializedName("name")
    private String name;

    public Auth(Context context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.email = mPrefs.getString("profile_email_text", null);
        this.name = mPrefs.getString("profile_name_text", null);
        this.deviceId = AppHelper.getUniqueDeviceId(context);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "Auth{" +
                ", email='" + email + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
