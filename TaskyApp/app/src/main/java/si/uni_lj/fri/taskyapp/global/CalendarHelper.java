package si.uni_lj.fri.taskyapp.global;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by urgas9 on 24.3.16, OpenHours.com
 */
public class CalendarHelper {

    private static final String TAG = "CalendarHelper";
    public static ArrayList<String> nameOfEvent = new ArrayList<>();
    public static ArrayList<String> startDates = new ArrayList<>();
    public static ArrayList<String> endDates = new ArrayList<>();
    public static ArrayList<String> descriptions = new ArrayList<>();

    public static ArrayList<String> readCalendarEvent(Context context) {
        if(!PermissionsHelper.hasPermission(context, Manifest.permission.READ_CALENDAR)){
           Log.e(TAG, "Cannot read calendar events as permission is not granted!");
            return nameOfEvent;
        }
        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/events"),
                        new String[] { "calendar_id", "title", "description",
                                "dtstart", "dtend", "eventLocation" }, "dtstart > ? AND (dtend IS NULL OR dtend < ?",
                        null, null);
        if(cursor == null){
            Log.e(TAG, "Cursor is null!");
            return new ArrayList<>();
        }
        cursor.moveToFirst();
        // fetching calendars name
        String CNames[] = new String[cursor.getCount()];

        // fetching calendars id
        nameOfEvent.clear();
        startDates.clear();
        endDates.clear();
        descriptions.clear();
        for (int i = 0; i < CNames.length; i++) {

            Log.d(TAG, "Cursor row: " + cursor.getString(0) + ";-; " + cursor.getString(1) + ";-; "
                    + cursor.getString(2) + ";-; " + cursor.getString(3) + ";-; " + cursor.getString(4));
            nameOfEvent.add(cursor.getString(1));
            startDates.add(getDate(Long.parseLong(cursor.getString(3))));
            if(cursor.getString(4) != null) {
                endDates.add(getDate(Long.parseLong(cursor.getString(4))));
            }
            descriptions.add(cursor.getString(2));
            CNames[i] = cursor.getString(1);
            cursor.moveToNext();

        }
        cursor.close();
        return nameOfEvent;
    }

    public static String getEventNameAtTime(Context context, long timestamp){
        if(!PermissionsHelper.hasPermission(context, Manifest.permission.READ_CALENDAR)){
            Log.e(TAG, "Cannot read calendar events as permission is not granted!");
            return null;
        }
        String whereArg = Long.toString(timestamp);
        /*Cursor cursor = context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/events"),
                        new String[] { "calendar_id", "title", "description",
                                "dtstart", "dtend", "eventLocation", "duration" }, "dtstart <= ? AND (dtend IS NULL OR dtend >= ?)",
                        new String[]{whereArg, whereArg}, null);*/
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
        ContentUris.appendId(eventsUriBuilder, timestamp);
        ContentUris.appendId(eventsUriBuilder, timestamp);
        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = null;
        cursor = context.getContentResolver().query(eventsUri, new String[] { "calendar_id", "title", "description",
                "dtstart", "dtend", "eventLocation", "duration" }, null, null, CalendarContract.Instances.DTSTART + " ASC");
        if(cursor == null){
            Log.e(TAG, "Cursor is null!");
            return null;
        }
        cursor.moveToFirst();
        // fetching calendars name
        String CNames[] = new String[cursor.getCount()];

        // fetching calendars id
        ArrayList<String> eventNameArray = new ArrayList<>();
        for (int i = 0; i < CNames.length; i++) {

            Log.d(TAG, "Cursor row: " + cursor.getString(0) + ";-; " + cursor.getString(1) + ";-; "
                    + cursor.getString(2) + ";-; " + cursor.getString(3) + ";-; " + cursor.getString(4) + ";-; " + cursor.getString(6));
            eventNameArray.add(cursor.getString(1));
            cursor.moveToNext();

        }
        cursor.close();
        if(eventNameArray.isEmpty()){
            return null;
        }
        return eventNameArray.get(0);
    }

    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                "dd/MM/yyyy hh:mm:ss a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}