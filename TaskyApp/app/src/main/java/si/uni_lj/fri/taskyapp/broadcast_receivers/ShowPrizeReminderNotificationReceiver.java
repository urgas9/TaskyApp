package si.uni_lj.fri.taskyapp.broadcast_receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

import si.uni_lj.fri.taskyapp.MainActivity;
import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.data.OfficeHoursObject;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 19-Feb-16, OpenHours.com
 */
public class ShowPrizeReminderNotificationReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "ShowNotifToUserReceiver";
    SharedPreferences mPrefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        OfficeHoursObject officeHoursObject = new OfficeHoursObject(context);
        if(officeHoursObject.areNowOfficeHours()){
            Log.d(LOG_TAG, "Should show a prize reminder notification.");

            Calendar c = Calendar.getInstance();
            long timeDifferenceSinceLastNotification = c.getTimeInMillis() - mPrefs.getLong(Constants.PREFS_PRIZE_NOTIFICATION_REMINDER_LAST_SENT, 0);
            final long MIN_TIME_DIFFERENCE_TWO_NOTIFS = 20 * 60 * 60 * 1000;
            if(timeDifferenceSinceLastNotification > MIN_TIME_DIFFERENCE_TWO_NOTIFS){
                Intent notifIntent = new Intent(context, MainActivity.class);
                notifIntent.putExtra("notification_prize_reminder", Constants.SHOW_NOTIFICATION_PRIZE_REMINDER_ID);
                PendingIntent pi = PendingIntent.getActivity(context, Constants.SHOW_NOTIFICATION_REQUEST_CODE, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AppHelper.showNotification(context, "[RECEIVER] " + context.getString(R.string.notif_prize_reminder_message), pi, Constants.SHOW_NOTIFICATION_PRIZE_REMINDER_ID);
                mPrefs.edit().putLong(Constants.PREFS_PRIZE_NOTIFICATION_REMINDER_LAST_SENT, c.getTimeInMillis()).apply();
            }

        }
    }

}
