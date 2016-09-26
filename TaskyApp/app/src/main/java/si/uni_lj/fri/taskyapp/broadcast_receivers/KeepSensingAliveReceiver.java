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

package si.uni_lj.fri.taskyapp.broadcast_receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
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
        AppHelper.printExtras(intent);
        mSensingInitiator.senseWithDefaultSensingConfiguration();

        AppHelper.startNotificationsAlarm(context);

        synchronized (o) {
            // Setting recovery alarm
            Intent recoveryIntent = new Intent();
            recoveryIntent.setAction(Constants.ACTION_KEEP_SENSING_ALIVE);
            recoveryIntent.putExtra("source", "recovery_intent");

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR,
                    AlarmManager.INTERVAL_HALF_HOUR,
                    PendingIntent
                            .getBroadcast(context, Constants.REQUEST_CODE_KEEP_ALIVE_ALARM, recoveryIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }
}
