package si.uni_lj.fri.taskyapp.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by urgas9 on 2. 01. 2016.
 */
public class NewSensorReadingReceiver extends BroadcastReceiver {

    private TextView mStatusTextView;

    public NewSensorReadingReceiver(TextView statusTextView) {
        super();
        mStatusTextView = statusTextView;
    }

    //Broadcast receiver
    @Override
    public void onReceive(Context context, Intent intent) {
        //Add current time
        Calendar rightNow = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");
        String strDate = sdf.format(rightNow.getTime());
        ;
        String v = strDate + " " +
                intent.getStringExtra("activity") + " " +
                "Confidence : " + intent.getExtras().getInt("confidence") + "\n";

        v = mStatusTextView.getText() + v;
        mStatusTextView.setText(v);
    }
}
