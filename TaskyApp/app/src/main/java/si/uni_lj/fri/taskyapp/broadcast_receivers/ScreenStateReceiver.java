package si.uni_lj.fri.taskyapp.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import si.uni_lj.fri.taskyapp.service.ScreenStateService;

/**
 * Created by urgas9 on 16-Feb-16, OpenHours.com
 */
public class ScreenStateReceiver extends BroadcastReceiver {

    private boolean screenOn;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenOn = false;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenOn = true;
        }
        Intent i = new Intent(context, ScreenStateService.class);
        i.putExtra("screen_state", screenOn);
        context.startService(i);
    }

}