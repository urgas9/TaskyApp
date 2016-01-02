package si.uni_lj.fri.taskyapp.global;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by urgas9 on 31. 12. 2015.
 */
public class AppHelper {

    //Check for Google play services available on device
    public static boolean isPlayServiceAvailable(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

}
