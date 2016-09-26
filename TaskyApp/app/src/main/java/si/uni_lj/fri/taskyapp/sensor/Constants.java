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

package si.uni_lj.fri.taskyapp.sensor;

/**
 * Created by urgas9 on 10. 01. 2016.
 */
public class Constants {
    public static final long APPROXIMATE_INTERVAL_MILLIS = 1000 * 60 * 20;//30000;
    public static final long MIN_INTERVAL_MILLIS = 1000 * 60 * 10; // The minimum time required between two sensing
    public static final long MAX_INTERVAL_WITHOUT_SENSING_DATA_IN_MILLIS = 1000 * 60 * 30;
    public static final long SENSING_WINDOW_LENGTH_MILLIS = 10 * 1000;
    public static final String PREFS_LAST_LOC_LAT = "PREFS_LAST_LOC_LAT";
    public static final String PREFS_LAST_LOC_LNG = "PREFS_LAST_LOC_LNG";
    public static final String PREFS_LAST_LOC_ACCURACY = "PREFS_LAST_LOC_ACCURACY";
    public static final String PREFS_SHOW_LEADERBOARD_MSG = "PREFS_SHOW_LEADERBOARD_MSG";
    public static final String PREFS_OFFICE_HOURS = "profile_office_hours_text";
    public static final String PREFS_NOT_IN_OFFICE_TODAY_TIMESTAMP = "PREFS_NOT_IN_OFFICE_TODAY_TIMESTAMP";
    public static final String PREFS_PRIZE_NOTIFICATION_REMINDER_LAST_SENT = "PREFS_PRIZE_NOTIFICATION_REMINDER_LAST_SENT";
    public static final String PREFS_LABEL_DETECTED_TASK_NOTIFICATION_REMINDER_LAST_SENT = "PREFS_LABEL_DETECTED_TASK_NOTIFICATION_REMINDER_LAST_SENT";
    public static final String PREFS_NUM_OF_LABEL_TASK_NOTIFICATION_REMINDERS_SENT = "PREFS_NUM_OF_LABEL_TASK_NOTIFICATION_REMINDERS_SENT";
    public static final int LOCATION_ACCURACY_AT_LEAST = 200;
    public static final int LOCATION_MIN_DISTANCE_TO_LAST_LOC = 35;
    public static final String ACTION_NEW_SENSOR_READING = "si.uni_lj.fri.taskyapp.NewSensorReading";
    public static final String ACTION_NEW_SENSOR_READING_RECORD = "si.uni_lj.fri.taskyapp.NewSensorReadingRecord";
    public static final String ACTION_KEEP_SENSING_ALIVE = "si.uni_lj.fri.taskyapp.KeepAliveAction";
    public static final String ACTION_NOTIFICATION_CLICK_ACTION = "si.uni_lj.fri.taskyapp.NotificationClickAction";
    public static final String DATE_FORMAT_TO_SHOW_DAY = "EEE, MM yyyy ";
    public static final String DATE_FORMAT_TO_SHOW_FULL = "HH:mm:ss dd/MM/yyyy ";
    public static final String PREFS_LAST_SCREEN_STATE = "PREFS_LAST_SCREEN_STATE";
    public static final String PREFS_LAST_SCREEN_STATE_TIME = "PREFS_LAST_SCREEN_STATE_TIME";
    public static final String PREFS_LAST_TIME_SENT_TO_SERVER = "PREFS_LAST_TIME_SENT_TO_SERVER";
    public static final int MAX_INTERVAL_BETWEEN_TWO_SERVER_POSTS = 1000 * 60 * 60 * 4;
    public static final String PREFS_WENT_THROUGH_TUTORIAL_SPLASH = "PREFS_WENT_THROUGH_TUTORIAL_SPLASH";
    public static final int LABEL_TASK_REQUEST_CODE = 1000;
    public static final int SHOW_NOTIFICATION_REMINDER_ID = 901;
    public static final int SHOW_NOTIFICATION_JUST_SENSED_ID = 902;
    public static final int SHOW_NOTIFICATION_PRIZE_REMINDER_ID = 903;
    public static final int SHOW_NOTIFICATION_LABEL_LAST_ID = 904;
    public static final int SHOW_NOTIFICATION_REQUEST_CODE = 900;
    public static final int REQUEST_CODE_KEEP_SENSING_ALIVE = 31;
    public static final int REQUEST_CODE_KEEP_ALIVE_ALARM = 20;
    public static final int REQUEST_CODE_ACTIVITY_UPDATES = 21;
    public static final int REQUEST_CODE_ALARM_UPDATES = 22;
    public static final int REQUEST_CODE_LOCATION_UPDATES = 23;
    public static final String PREFS_CHOSEN_WEARABLE_NAME = "PREFS_CHOSEN_WEARABLE_NAME";
    public static final String PREFS_CHOSEN_WEARABLE_MAC = "PREFS_CHOSEN_WEARABLE_MAC";
    public static int NUM_OF_RANDOMLY_LABEL_NOTIFICATIONS_TO_SEND = 3;
    public static String ACTION_NOTIF_BTN_NOT_IN_OFFICE = "not_in_office";

}
