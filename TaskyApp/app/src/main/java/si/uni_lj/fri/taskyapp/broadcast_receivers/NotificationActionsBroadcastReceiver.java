package si.uni_lj.fri.taskyapp.broadcast_receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;

import si.uni_lj.fri.taskyapp.data.OfficeHoursObject;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.sensor.Constants;

public class NotificationActionsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "NotifActionsBrodcstRcvr";

    public NotificationActionsBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "Inside notification actions broadcast...");
        if (intent != null && intent.getExtras() != null) {
            Bundle b = intent.getExtras();
            String action = b.getString("action");
            int notificationId = b.getInt("notif_id");
            Log.e(TAG, "Handling notification action: " + action + " and notification id: " + notificationId);

            if (action != null && action.equals(Constants.ACTION_NOTIF_BTN_NOT_IN_OFFICE)) {
                handleActionNotInOfficeToday(context);
            }
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
    }

    private void handleActionNotInOfficeToday(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        int deleted = SensorReadingRecord.deleteAll(SensorReadingRecord.class,
                "time_started_sensing > ? AND label <= 0", "" + calendar.getTimeInMillis());

        Log.d(TAG, "Not in office today, deleted today's unlabeled records: " + deleted);

        OfficeHoursObject.setNotInOfficeToday(context);

    }
}
