package si.uni_lj.fri.taskyapp.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import si.uni_lj.fri.taskyapp.global.AppHelper;

/**
 * Created by urgas9 on 19-Feb-16, OpenHours.com
 */
public class ShowNotificationToUserReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "ShowNotifToUserReceiver";
    SharedPreferences mPrefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        AppHelper.showNotification(context);
    }

}
