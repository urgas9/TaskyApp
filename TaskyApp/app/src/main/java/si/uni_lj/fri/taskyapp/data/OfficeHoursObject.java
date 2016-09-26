/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This library was developed as part of the paper submitted for the UbitTention workshop paper (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package si.uni_lj.fri.taskyapp.data;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import si.uni_lj.fri.taskyapp.MainActivity;
import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 31.3.16, OpenHours.com
 */
public class OfficeHoursObject {

    private static final int TIMERANGE_DIFFERENCE_ATLEAST_MINUTES = 4 * 60;
    boolean weekendsIncluded;
    private int hoursStart;
    private int minutesStart;
    private int hoursEnd;
    private int minutesEnd;
    private long lastTimeReportedNotInOffice;

    public OfficeHoursObject(Context context) {
        super();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String savedOfficeHours = prefs.getString(Constants.PREFS_OFFICE_HOURS, context.getString(R.string.pref_default_office_hours));

        String[] time = savedOfficeHours.split(" - ");
        if (time.length == 2) {
            parseStartAndEndTime(time[0], time[1]);
        }

        weekendsIncluded = areInOfficeForWeekends(context);
        lastTimeReportedNotInOffice = lastTimeReportedNotInOffice(context);
    }

    public OfficeHoursObject(String timeRangeString) {
        if (timeRangeString != null) {
            String[] times = timeRangeString.split(" - ");
            if (times.length == 2) {
                parseStartAndEndTime(times[0], times[1]);
            }
        }
    }

    public static boolean validateAndSaveOfficeHoursString(Context mContext, String stringValue) {
        stringValue = prettifyTimeRangeStringValue(stringValue);
        if (!OfficeHoursObject.isStringValid(stringValue)) {
            if (mContext != null) {
                Toast.makeText(mContext, "Please enter a valid time range (example: 08:00 - 16:00)!", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        OfficeHoursObject tre = new OfficeHoursObject(stringValue);
        if (!tre.isTimeDifferenceBigEnough()) {
            if (mContext != null) {
                Toast.makeText(mContext, String.format(mContext.getString(R.string.time_range_too_short), stringValue), Toast.LENGTH_LONG).show();
            }
            return false;
        }
        stringValue = tre.toString();
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(Constants.PREFS_OFFICE_HOURS, stringValue).commit();
        return true;
    }

    public static String prettifyTimeRangeStringValue(String stringValue) {
        if (!stringValue.contains(" - ") && stringValue.contains("-")) {
            Log.d("TAG", "we have a fucking match! " + stringValue);
            stringValue = stringValue.replaceAll(" ", "").replaceAll("-", " - ");
            Log.d("TAG", "new value: " + stringValue);

        }
        return new OfficeHoursObject(stringValue).toString();
    }

    public static boolean isStringValid(String timeRangeString) {
        return timeRangeString.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9] - ([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    public static boolean areInOfficeForWeekends(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("profile_office_hours_include_weekends", false);
    }

    public static void saveWeekendsDecision(Context context, boolean includeWeekends) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean("profile_office_hours_include_weekends", includeWeekends)
                .apply();
    }

    public static void setNotInOfficeToday(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(Constants.PREFS_NOT_IN_OFFICE_TODAY_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    public boolean isTimeDifferenceBigEnough() {
        return (hoursEnd * 60 + minutesEnd - (hoursStart * 60 + minutesStart)) > TIMERANGE_DIFFERENCE_ATLEAST_MINUTES;
    }

    public boolean areNowOfficeHours() {
        Calendar c = Calendar.getInstance();
        int timeMinsNow = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        int timeMinsStart = hoursStart * 60 + minutesStart;
        int timeMinsEnd = hoursEnd * 60 + minutesEnd;

        // Check if user has reported that he is not in office today
        Calendar notOfficeCal = Calendar.getInstance();
        notOfficeCal.setTimeInMillis(lastTimeReportedNotInOffice);
        if (c.get(Calendar.DAY_OF_YEAR) == notOfficeCal.get(Calendar.DAY_OF_YEAR)
                && c.get(Calendar.YEAR) == notOfficeCal.get(Calendar.YEAR)) {
            return false;
        }
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        boolean isWeekendToday = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
        return (!isWeekendToday || weekendsIncluded) && timeMinsNow >= timeMinsStart && timeMinsNow <= timeMinsEnd;
    }

    private void parseStartAndEndTime(String startHoursMinsSeparatedByColon, String endHoursMinsSeparatedByColon) {
        if (startHoursMinsSeparatedByColon != null) {
            String[] hoursMins = startHoursMinsSeparatedByColon.split(":");
            if (hoursMins.length == 2) {
                this.hoursStart = Integer.parseInt(hoursMins[0]);
                this.minutesStart = Integer.parseInt(hoursMins[1]);
            }
        }
        if (endHoursMinsSeparatedByColon != null) {
            String[] hoursMins = endHoursMinsSeparatedByColon.split(":");
            if (hoursMins.length == 2) {
                this.hoursEnd = Integer.parseInt(hoursMins[0]);
                this.minutesEnd = Integer.parseInt(hoursMins[1]);
            }
        }
    }

    public double getPercentageOfWorkDone() {
        Calendar c = Calendar.getInstance();
        int absMinutesStart = hoursStart * 60 + minutesStart;
        int minutesDifference = hoursEnd * 60 + minutesEnd - absMinutesStart;
        int minutesNow = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        return (minutesNow - absMinutesStart) / ((double) minutesDifference);
    }

    public int getMinutesTimeOfTheDayToShowReminder() {
        int absMinutesStart = hoursStart * 60 + minutesStart;
        int minutesDifference = hoursEnd * 60 + minutesEnd - absMinutesStart;

        return (int) (absMinutesStart + minutesDifference * 0.3);
    }

    public void showReminderPrizeNotification(Context context) {
        if (!areNowOfficeHours()) {
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar c = Calendar.getInstance();
        long timeDifferenceSinceLastNotification = c.getTimeInMillis() - prefs.getLong(Constants.PREFS_PRIZE_NOTIFICATION_REMINDER_LAST_SENT, 0);
        final long MIN_TIME_DIFFERENCE_TWO_NOTIFS = 23 * 60 * 60 * 1000;


        int absMinutesStart = hoursStart * 60 + minutesStart;
        int minutesDifference = hoursEnd * 60 + minutesEnd - absMinutesStart;

        int minutesNow = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        double percentOfOfficeHours = (minutesNow - absMinutesStart) / ((double) minutesDifference);
        Log.d("OfficeHours", "Daily percentage worked: " + percentOfOfficeHours * 100);
        if (percentOfOfficeHours > 0.3 && timeDifferenceSinceLastNotification > MIN_TIME_DIFFERENCE_TWO_NOTIFS) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("notification_prize_reminder", Constants.SHOW_NOTIFICATION_PRIZE_REMINDER_ID);
            PendingIntent pi = PendingIntent.getActivity(context, Constants.SHOW_NOTIFICATION_REQUEST_CODE, intent, 0);
            AppHelper.showNotification(context, context.getString(R.string.notif_prize_reminder_message), pi, Constants.SHOW_NOTIFICATION_PRIZE_REMINDER_ID);
            prefs.edit().putLong(Constants.PREFS_PRIZE_NOTIFICATION_REMINDER_LAST_SENT, c.getTimeInMillis()).apply();
        }

    }

    @Override
    public String toString() {
        return String.format("%02d:%02d - %02d:%02d", hoursStart, minutesStart, hoursEnd, minutesEnd);
    }

    private long lastTimeReportedNotInOffice(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(Constants.PREFS_NOT_IN_OFFICE_TODAY_TIMESTAMP, 0);
    }
}
