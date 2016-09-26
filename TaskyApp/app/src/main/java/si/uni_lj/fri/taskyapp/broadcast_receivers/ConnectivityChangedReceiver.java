/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This project was developed as part of the paper submitted for the UbitTention workshop paper (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package si.uni_lj.fri.taskyapp.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import si.uni_lj.fri.taskyapp.sensor.Constants;
import si.uni_lj.fri.taskyapp.service.SendDataToServerIntentService;

/**
 * Created by urgas9 on 20-Feb-16, OpenHours.com
 */
public class ConnectivityChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectivityChangedRece";
    private static boolean firstEvent = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d(TAG, "Network connectivity change");
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (intent.getExtras() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();

            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED && ni.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.i(TAG, "Network " + ni.getTypeName() + " connected");

                long lastTimestamp = PreferenceManager.getDefaultSharedPreferences(context).getLong(Constants.PREFS_LAST_TIME_SENT_TO_SERVER, 0);
                if ((lastTimestamp + Constants.MAX_INTERVAL_BETWEEN_TWO_SERVER_POSTS) <= System.currentTimeMillis()) {
                    context.startService(new Intent(context, SendDataToServerIntentService.class));
                }

            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d(TAG, "There's no network connectivity");
            }
        }
    }
}
