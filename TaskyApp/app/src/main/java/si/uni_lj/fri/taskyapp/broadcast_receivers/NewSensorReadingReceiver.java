/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This project was developed as part of the paper submitted for the UbitTention workshop (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package si.uni_lj.fri.taskyapp.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by urgas9 on 2. 01. 2016.
 */
public class NewSensorReadingReceiver extends BroadcastReceiver {


    public NewSensorReadingReceiver() {
        super();
    }

    //Broadcast receiver
    @Override
    public void onReceive(Context context, Intent intent) {
        //Add current time
        Calendar rightNow = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");
        String strDate = sdf.format(rightNow.getTime());
        String policy = intent.getStringExtra("policy");
        if (policy.equals("activity")) {

            String v = strDate + " " +
                    intent.getStringExtra("activity") + " " +
                    "Confidence : " + intent.getExtras().getInt("confidence") + "\n";

            //v = mStatusTextView.getText() + v;
            //mStatusTextView.setText(v);
        } else if (policy.equals("location")) {
            Location loc = intent.getParcelableExtra("location");
            String v = strDate + " Location : " + loc.getLatitude() + ", " + loc.getLongitude() + "\n";
            //v = mStatusTextView.getText() + v;
            //mStatusTextView.setText(v);
        }
    }
}
