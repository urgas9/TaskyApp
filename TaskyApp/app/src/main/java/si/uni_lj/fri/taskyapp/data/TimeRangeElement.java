package si.uni_lj.fri.taskyapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 31.3.16, OpenHours.com
 */
public class TimeRangeElement {

    private static final int TIMERANGE_DIFFERENCE_ATLEAST_MINUTES = 4*60;
    private int hoursStart;
    private int minutesStart;
    private int hoursEnd;
    private int minutesEnd;

    public TimeRangeElement(Context context){
        super();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String savedOfficeHours = prefs.getString(Constants.PREFS_OFFICE_HOURS, context.getString(R.string.pref_default_office_hours));

        String[] time = savedOfficeHours.split(" - ");
        if(time.length == 2){
            parseStartAndEndTime(time[0], time[1]);
        }
    }

    public TimeRangeElement(String timeRangeString){
        if(timeRangeString != null){
            String[] times = timeRangeString.split(" - ");
            if(times.length == 2){
                parseStartAndEndTime(times[0], times[1]);
            }
        }
    }
    public TimeRangeElement(String startHoursMinsSeparatedByColon, String endHoursMinsSeparatedByColon){
        super();
        parseStartAndEndTime(startHoursMinsSeparatedByColon, endHoursMinsSeparatedByColon);
    }

    public TimeRangeElement(int hoursStart, int minutesStart, int hoursEnd, int minutesEnd){
        super();
        this.hoursStart = hoursStart;
        this.minutesStart = minutesStart;
        this.hoursEnd = hoursEnd;
        this.minutesEnd = minutesEnd;
    }

    public boolean isTimeDifferenceBigEnough(){
        return (hoursEnd*60 + minutesEnd - (hoursStart*60 + minutesStart)) > TIMERANGE_DIFFERENCE_ATLEAST_MINUTES;
    }

    public boolean areNowOfficeHours(){
        Calendar c = Calendar.getInstance();
        int hoursNow = c.get(Calendar.HOUR_OF_DAY);
        int minutesNow = c.get(Calendar.MINUTE);
        return hoursStart >= hoursNow && minutesStart >= minutesNow &&
                hoursEnd <= hoursNow && minutesEnd <= minutesNow;
    }
    private void parseStartAndEndTime(String startHoursMinsSeparatedByColon, String endHoursMinsSeparatedByColon){
        if(startHoursMinsSeparatedByColon != null) {
            String[] hoursMins = startHoursMinsSeparatedByColon.split(":");
            if(hoursMins.length == 2){
                this.hoursStart = Integer.parseInt(hoursMins[0]);
                this.minutesStart = Integer.parseInt(hoursMins[1]);
            }
        }
        if(endHoursMinsSeparatedByColon != null) {
            String[] hoursMins = endHoursMinsSeparatedByColon.split(":");
            if(hoursMins.length == 2){
                this.hoursEnd = Integer.parseInt(hoursMins[0]);
                this.minutesEnd = Integer.parseInt(hoursMins[1]);
            }
        }
    }

    public static boolean isStringValid(String timeRangeString){
        return timeRangeString.matches("^[0-9]{1,2}:[0-9]{1,2} - [0-9]{1,2}:[0-9]{1,2}$");
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d - %02d:%02d", hoursStart, minutesStart, hoursEnd, minutesEnd);
    }
}
