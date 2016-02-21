package si.uni_lj.fri.taskyapp.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import si.uni_lj.fri.taskyapp.service.SendDataToServerService;

/**
 * Created by urgas9 on 20-Feb-16, OpenHours.com
 */
public class ConnectivityChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectivityChangedRece";
    private static boolean firstEvent = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Network connectivity change");
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (intent.getExtras() != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();

            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED && ni.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.i(TAG, "Network " + ni.getTypeName() + " connected");
                context.startService(new Intent(context, SendDataToServerService.class));
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d(TAG, "There's no network connectivity");
            }
        }
    }
}
