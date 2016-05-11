package si.uni_lj.fri.taskyapp.broadcast_receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;
import si.uni_lj.fri.taskyapp.sensor.SensingInitiator;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class KeepSensingAliveReceiver extends BroadcastReceiver {

    private static final String TAG = "KeepSensingAliveRecvr";
    Object o = new Object();
    private SensingInitiator mSensingInitiator;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "TaskyApp onReceive, system started");
        if (mSensingInitiator == null) {
            mSensingInitiator = new SensingInitiator(context);
        }
        mSensingInitiator.senseWithDefaultSensingConfiguration();

        AppHelper.startNotificationsAlarm(context);

        synchronized (o) {
            // Setting recovery alarm
            Intent recoveryIntent = new Intent();
            recoveryIntent.setAction(Constants.ACTION_KEEP_SENSING_ALIVE);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    Constants.APPROXIMATE_INTERVAL_MILLIS,
                    Constants.APPROXIMATE_INTERVAL_MILLIS,
                    PendingIntent
                            .getBroadcast(context, 20, recoveryIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }
}
