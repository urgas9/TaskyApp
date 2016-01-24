package si.uni_lj.fri.taskyapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import si.uni_lj.fri.taskyapp.sensor.SensingInitiator;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class AfterStartupReceiver extends BroadcastReceiver{

    private static final String TAG = "AfterStartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "TaskyApp onReceive, system started");
        SensingInitiator si = new SensingInitiator(context);
        si.senseWithDefaultSensingConfiguration();
    }
}
