/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This library was developed as part of the paper submitted for the UbitTention workshop paper (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

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
