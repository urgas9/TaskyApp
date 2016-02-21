package si.uni_lj.fri.taskyapp.broadcast_receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import si.uni_lj.fri.taskyapp.ListDataActivity;
import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.sensor.Constants;

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
        showNotification(context);
    }

    public void showNotification(Context context) {
        Intent intent = new Intent(context, ListDataActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, Constants.SHOW_NOTIFICATION_REQUEST_CODE, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Would you mind labelling your daily tasks?");
        mBuilder.setContentIntent(pi);
        if (mPrefs.getBoolean("notifications_new_message_vibrate", false)) {
            mBuilder.setVibrate(new long[]{700, 700, 700});
        }
        mBuilder.setLights(Color.RED, 2000, 2000);
        String notifSound = mPrefs.getString("notifications_new_message_ringtone", null);
        Log.d(LOG_TAG, "Sound: " + notifSound);
        if (notifSound != null) {
            mBuilder.setSound(Uri.parse(notifSound));
        }
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.SHOW_NOTIFICATION_REQUEST_CODE, mBuilder.build());
    }
}
