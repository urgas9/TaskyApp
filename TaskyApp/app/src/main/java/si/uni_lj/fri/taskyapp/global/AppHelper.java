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

package si.uni_lj.fri.taskyapp.global;

import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.orm.SugarRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import si.uni_lj.fri.taskyapp.BuildConfig;
import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.broadcast_receivers.ShowPrizeReminderNotificationReceiver;
import si.uni_lj.fri.taskyapp.data.OfficeHoursObject;
import si.uni_lj.fri.taskyapp.data.db.DailyAggregatedData;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 31. 12. 2015.
 */
public class AppHelper {

    //Check for Google play services available on device
    public static boolean isPlayServiceAvailable(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

    public static SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    public static double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    public static boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isValidEmail(String target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * Checks for a flag in string resources file if debug is enabled or not
     *
     * @return
     */
    public static boolean isDebugEnabled() {
        return BuildConfig.DEBUG; // show_debug value is generated by gradle, check buildTypes
    }

    public static String getUniqueDeviceId(Context context) {
        return android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

    public static boolean isConnectedToWifi(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static void showExplainNotificationsDialog(Activity activity) {
        String[] taskComplexities = activity.getResources().getStringArray(R.array.task_difficulties_array);
        String[] taskDescriptions = activity.getResources().getStringArray(R.array.task_difficulties_description_array);

        String resultString = "<html>";
        for (int i = 1; i < taskComplexities.length; i++) {
            resultString += "<b>&#8226; " + taskComplexities[i] + "</b>: " + taskDescriptions[i] + "<br />";
        }
        resultString += "</html>";
        new MaterialDialog.Builder(activity)
                .content(Html.fromHtml(resultString))
                .title(R.string.task_descriptions)
                .positiveText(R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public static int dpToPx(Context mContext, int dp) {
        if (mContext == null) {
            return 0;
        }
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int getTaskColor(Context ctx, int label) {
        if (label > 0 && ctx != null) {
            return (Integer) new ArgbEvaluator()
                    .evaluate(label / 5.f, Color.GREEN, Color.RED);
        } else {
            return ContextCompat.getColor(ctx, R.color.primary);
        }
    }

    public static BitmapDescriptor getMarkerIcon(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    public static Calendar getCalendarAtMidnight(int relativeDayToToday) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, relativeDayToToday);
        return calendar;
    }

    public static List<DailyAggregatedData> aggregateDailyData() {
        final String TAG = "aggregateDailyData";

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        List<SensorReadingRecord> sensorReadings = SensorReadingRecord.find(SensorReadingRecord.class,
                "time_started_sensing <= ?", new String[]{"" + calendar.getTimeInMillis()}, null, "time_started_sensing ASC", null);

        List<DailyAggregatedData> resultsList = new LinkedList<>();

        int sumLabels = 0, countDailyTasks = 0, countLabels = 0, lastDay = -1, countAll = 0, allRecords = sensorReadings.size();
        for (SensorReadingRecord srr : sensorReadings) {
            countAll++;
            calendar.setTimeInMillis(srr.getTimeStartedSensing());
            if (calendar.get(Calendar.DAY_OF_YEAR) != lastDay || countAll == allRecords) {
                long count = SugarRecord.count(DailyAggregatedData.class, " day_of_year = ?", new String[]{"" + calendar.get(Calendar.DAY_OF_YEAR)});
                if (lastDay > 0 &&
                        count == 0L) {
                    DailyAggregatedData dad = new DailyAggregatedData();
                    dad.setDayOfYear(calendar.get(Calendar.DAY_OF_YEAR));
                    dad.setAllReadings(countDailyTasks);
                    dad.setCountLabeled(countLabels);
                    dad.setAverageLabel((sumLabels / (double) countLabels));
                    Log.d(TAG, "Saving: " + dad);
                    resultsList.add(dad);
                    SugarRecord.save(dad);
                } else {
                    Log.d(TAG, "First iteration or record already exists.");
                }
                lastDay = calendar.get(Calendar.DAY_OF_YEAR);
                sumLabels = countLabels = countDailyTasks = 0;
            }
            if (srr.getLabel() != null && srr.getLabel() > 0) {

                sumLabels += srr.getLabel();
                countLabels++;
            }
            countDailyTasks++;
        }

        Log.d(TAG, "Finished with daily data aggregation.");
        return resultsList;
    }

    public static void showNotification(Context context, String message, PendingIntent pendingIntent, int notifId) {

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notifications_white_24dp)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setContentIntent(pendingIntent);
        if (mPrefs.getBoolean("notifications_new_message_vibrate", false)) {
            mBuilder.setVibrate(new long[]{100, 500, 100});
        }
        mBuilder.setLights(Color.CYAN, 2000, 2000);
        String notifSound = mPrefs.getString("notifications_new_message_ringtone", null);
        if (notifSound != null) {
            mBuilder.setSound(Uri.parse(notifSound));
        }
        mBuilder.setAutoCancel(true);
        boolean notInOfficeToday = true;
        if (notInOfficeToday) {
            mBuilder.addAction(R.drawable.ic_not_interested_black_24dp,
                    context.getString(R.string.not_in_office_today_notification),
                    getNotInOfficePendingIntent(context, notifId));
        }
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notifId, mBuilder.build());

    }

    private static PendingIntent getNotInOfficePendingIntent(Context context, int notifId) {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent();
        intent.putExtra("action", Constants.ACTION_NOTIF_BTN_NOT_IN_OFFICE);
        intent.putExtra("notif_id", notifId);
        intent.setAction(Constants.ACTION_NOTIFICATION_CLICK_ACTION);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static List<SensorReadingRecord> getSensorRecordsOfLastTwoDays() {
        Calendar calendarFrom = AppHelper.getCalendarAtMidnight(-1);
        return SensorReadingRecord.find(SensorReadingRecord.class,
                "time_started_sensing > ?", new String[]{"" + calendarFrom.getTimeInMillis()}, null, "time_started_sensing ASC", null);
    }

    public static void startNotificationsAlarm(Context context) {
        Calendar calendar = Calendar.getInstance();

        OfficeHoursObject officeHoursObject = new OfficeHoursObject(context);
        int minutesOfTheDay = officeHoursObject.getMinutesTimeOfTheDayToShowReminder();

        Log.e("NOTIFICATIONS_ALARM", "Setting it to: " + minutesOfTheDay / 60 + "h " + minutesOfTheDay % 60 + "mins");
        calendar.set(Calendar.HOUR_OF_DAY, minutesOfTheDay / 60);
        calendar.set(Calendar.MINUTE, minutesOfTheDay % 60);
        calendar.set(Calendar.SECOND, 0);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0,
                new Intent(context, ShowPrizeReminderNotificationReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }

    public static void printExtras(Intent i) {
        if (i == null) {
            return;
        }
        Bundle bundle = i.getExtras();
        if (bundle == null) {
            return;
        }
        Log.d("INTENT_EXTRAS", "++++ Printing extras: +++");
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            Log.d("INTENT_EXTRAS", String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }
    }
}
