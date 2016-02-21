package si.uni_lj.fri.taskyapp.networking;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.global.AppHelper;

/**
 * Created by urgas9 on 5. 01. 2016.
 */
public class ConnectionResponse<T> {

    private T content;
    private int responseCode; // If equal to 0, there were unknown problems, if negative, exception was caught

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (responseCode >= 400 && responseCode < 500) {
            return ctx.getString(R.string.trans_client_error);
        } else if (responseCode >= 500) {
            return ctx.getString(R.string.trans_server_error);
        } else {
            if (AppHelper.isDebugEnabled()) {
                return "Connection error with code " + responseCode;
            } else {
                return ctx.getString(R.string.trans_check_internet);
            }
        }
    }

    public String getDetailedResponseMessage(Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (!AppHelper.isNetworkAvailable(ctx)) {
            return ctx.getString(R.string.trans_check_internet);
        } else if (responseCode == 0 || responseCode >= 400) {
            return ctx.getString(R.string.trans_connection_error_detailed);
        } else {
            return "";
        }
    }

    public boolean isSuccess() {
        return responseCode == 200;
    }

    @Override
    public String toString() {
        return "NetworkResponse{" +
                "content='" + content + '\'' +
                ", responseCode=" + responseCode +
                '}';
    }
}
