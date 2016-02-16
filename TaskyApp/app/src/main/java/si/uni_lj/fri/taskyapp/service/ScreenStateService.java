package si.uni_lj.fri.taskyapp.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import si.uni_lj.fri.taskyapp.broadcast_receivers.ScreenStateReceiver;
import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 16-Feb-16, OpenHours.com
 */
public class ScreenStateService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        // REGISTER RECEIVER THAT HANDLES SCREEN ON AND SCREEN OFF LOGIC
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenStateReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ScreenStateService", "onStartCommand");
        if(intent.hasExtra("screen_state")) {
            boolean screenOn = intent.getBooleanExtra("screen_state", false);
            Log.d("ScreenStateService", "Saving screen state to: " + screenOn);
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            mPrefs.edit()
                    .putBoolean(Constants.PREFS_LAST_SCREEN_STATE, screenOn)
                    .putLong(Constants.PREFS_LAST_SCREEN_STATE_TIME, System.currentTimeMillis())
                    .apply();

        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}