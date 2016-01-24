package si.uni_lj.fri.taskyapp.global;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import si.uni_lj.fri.taskyapp.R;

/**
 * Created by urgas9 on 29. 10. 2015.
 * <p/>
 * This class includes helper methods for
 */
public class PermissionsHelper {

    public final static int REQUEST_LOCATION_PERMISSIONS_CODE = 232;
    public final static int REQUEST_WRITE_STORAGE_PERMISSIONS_CODE = 233;
    public final static int REQUEST_CALL_PERMISSIONS_CODE = 234;
    public final static int REQUEST_ACCOUNTS_PERMISSIONS_CODE = 235;

    public static boolean hasPermission(Context mContext, String permission) {
        return (ActivityCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean shouldWeAsk(Context ctx, String permission) {
        return (PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(permission, true));
    }

    public static void markAsAsked(Context ctx, String permission) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean(permission, false).apply();

    }

    public static String[] findUnAskedPermissions(Activity mActivity, String[] wanted) {

        ArrayList<String> result = new ArrayList<>();

        for (String perm : wanted) {

            if (!hasPermission(mActivity, perm) && shouldWeAsk(mActivity, perm)) {
                result.add(perm);
            }

        }

        return result.toArray(new String[result.size()]);

    }

    /**
     * Showing dialog to explain certain permissions
     *
     * @param fragmentOrActivity
     * @param permissions
     * @param requestCode
     */
    public static void showDialogToExplainPermission(final Object fragmentOrActivity, final String[] permissions, final int requestCode) {

        Fragment frag = null;
        Activity act = null;
        if (fragmentOrActivity instanceof Fragment) {
            frag = (Fragment) fragmentOrActivity;
        } else if (fragmentOrActivity instanceof Activity) {
            act = (Activity) fragmentOrActivity;
        }
        if (act == null && frag == null) {
            return;
        }

        Context ctx = act;
        if (frag != null) {
            ctx = frag.getContext();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);

        String content = null;
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE_PERMISSIONS_CODE:
                content = ctx.getString(R.string.permission_storage_denied);
                break;
            case REQUEST_CALL_PERMISSIONS_CODE:
                content = ctx.getString(R.string.permission_call_denied);
                break;
            case REQUEST_ACCOUNTS_PERMISSIONS_CODE:
                content = ctx.getString(R.string.permission_accounts_denied);
                break;
            default:
                Log.e("DialogExplainPermission", "Unhandled request code for dialog: " + requestCode);
                return;
        }
        final Fragment finalFrag = frag;
        final Activity finalAct = act;
        new MaterialDialog.Builder(ctx)
                .content(content)
                .positiveText(ctx.getString(R.string.ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        if (finalFrag != null) {
                            finalFrag.requestPermissions(permissions, requestCode);
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                finalAct.requestPermissions(permissions, requestCode);
                            }
                        }
                    }
                })
                .build()
                .show();
    }

    /**
     * Requesting storage permissions for Marshmallow or above
     *
     * @return
     */
    public static boolean requestStoragePermission(Object fragmentOrActivity) {
        Fragment frag = null;
        Activity act = null;
        Context ctx = null;
        if (fragmentOrActivity instanceof Fragment) {
            frag = (Fragment) fragmentOrActivity;
            ctx = frag.getContext();
        } else if (fragmentOrActivity instanceof Activity) {
            act = (Activity) fragmentOrActivity;
            ctx = act;
        }
        if (act == null && frag == null) {
            Log.e("requestStoragePermissin", "Object passed was neither of type Activity nor Fragment");
            return false;
        }


        /** Requesting permissions for Android Marshmallow and above **/
        final String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // Requesting permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionsHelper.hasPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (frag != null) {
                frag.requestPermissions(permissions, REQUEST_WRITE_STORAGE_PERMISSIONS_CODE);
            } else {
                act.requestPermissions(permissions, REQUEST_WRITE_STORAGE_PERMISSIONS_CODE);
            }
            return false;
        }
        return true;
    }

    /**
     * Requesting call permissions for Marshmallow or above
     *
     * @return
     */
    public static boolean requestCallPermission(Fragment fragment) {
        /** Requesting permissions for Android Marshmallow and above **/
        final String[] permissions = {Manifest.permission.CALL_PHONE};
        // Requesting permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionsHelper.hasPermission(fragment.getActivity(), Manifest.permission.CALL_PHONE)) {
            fragment.requestPermissions(permissions, REQUEST_CALL_PERMISSIONS_CODE);
            return false;
        }
        return true;
    }

    public static void requestLocationsPermissions(Activity a) {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        // Requesting permissions
        ActivityCompat.requestPermissions(a, permissions, REQUEST_LOCATION_PERMISSIONS_CODE);
    }

    public static void requestAllRequiredPermissions(Activity a){
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ActivityCompat.requestPermissions(a, permissions, REQUEST_LOCATION_PERMISSIONS_CODE);
    }

    /**
     * Requesting call permissions for Marshmallow or above (unused after not using G+ login anymore)
     *
     * @return
     */
    public static boolean requestAccountsPermission(Fragment fragment) {
        /** Requesting permissions for Android Marshmallow and above **/
        final String[] permissions = {Manifest.permission.GET_ACCOUNTS};
        // Requesting permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionsHelper.hasPermission(fragment.getActivity(), Manifest.permission.GET_ACCOUNTS)) {
            fragment.requestPermissions(permissions, REQUEST_ACCOUNTS_PERMISSIONS_CODE);
            return false;
        }
        return true;
    }

    /**
     * Open app settings and enable user to enable permissions for the app
     *
     * @param context
     */
    public static void openAppOSSettings(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

}
