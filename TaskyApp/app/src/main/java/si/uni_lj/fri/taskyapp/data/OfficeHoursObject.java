package si.uni_lj.fri.taskyapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 31.3.16, OpenHours.com
 */
public class OfficeHoursObject {

    private static final int TIMERANGE_DIFFERENCE_ATLEAST_MINUTES = 4 * 60;
    private int hoursStart;
    private int minutesStart;
    private int hoursEnd;
    private int minutesEnd;

    public OfficeHoursObject(Context context) {
        super();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String savedOfficeHours = prefs.getString(Constants.PREFS_OFFICE_HOURS, context.getString(R.string.pref_default_office_hours));

        String[] time = savedOfficeHours.split(" - ");
        if (time.length == 2) {
            parseStartAndEndTime(time[0], time[1]);
        }
    }

    public OfficeHoursObject(String timeRangeString) {
        if (timeRangeString != null) {
            String[] times = timeRangeString.split(" - ");
            if (times.length == 2) {
                parseStartAndEndTime(times[0], times[1]);
            }
        }
    }

    public OfficeHoursObject(String startHoursMinsSeparatedByColon, String endHoursMinsSeparatedByColon) {
        super();
        parseStartAndEndTime(startHoursMinsSeparatedByColon, endHoursMinsSeparatedByColon);
    }

    public OfficeHoursObject(int hoursStart, int minutesStart, int hoursEnd, int minutesEnd) {
        super();
        this.hoursStart = hoursStart;
        this.minutesStart = minutesStart;
        this.hoursEnd = hoursEnd;
        this.minutesEnd = minutesEnd;
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

    public boolean isTimeDifferenceBigEnough() {
        return (hoursEnd * 60 + minutesEnd - (hoursStart * 60 + minutesStart)) > TIMERANGE_DIFFERENCE_ATLEAST_MINUTES;
    }

    public boolean areNowOfficeHours() {
        Calendar c = Calendar.getInstance();
        int timeMinsNow = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        int timeMinsStart = hoursStart * 60 + minutesStart;
        int timeMinsEnd = hoursEnd * 60 + minutesEnd;

        return timeMinsNow >= timeMinsStart && timeMinsNow <= timeMinsEnd;
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

    @Override
    public String toString() {
        return String.format("%02d:%02d - %02d:%02d", hoursStart, minutesStart, hoursEnd, minutesEnd);
    }
}
