package si.uni_lj.fri.taskyapp.sensor;

/**
 * Created by urgas9 on 10. 01. 2016.
 */
public class Constants {
    public static final long APPROXIMATE_INTERVAL_MILLIS = 60000 * 30;//30000;
    public static final long MIN_INTERVAL_MILLIS = 1000 * 30 * 10; // Five mins is the minimum time required between two sensing
    public static final long MAX_INTERVAL_WITHOUT_SENSING_DATA_IN_MILLIS = (long)(APPROXIMATE_INTERVAL_MILLIS * 1.2);
    public static final long SENSING_WINDOW_LENGTH_MILLIS = 10 * 1000;

    public static final String PREFS_LAST_LOC_LAT = "PREFS_LAST_LOC_LAT";
    public static final String PREFS_LAST_LOC_LNG = "PREFS_LAST_LOC_LNG";
    public static final String PREFS_LAST_LOC_ACCURACY = "PREFS_LAST_LOC_ACCURACY";

    public static final int LOCATION_ACCURACY_AT_LEAST = 200;

    public static final int LOCATION_MIN_DISTANCE_TO_LAST_LOC = 70;
    public static final String ACTION_NEW_SENSOR_READING = "si.uni_lj.fri.taskyapp.NewSensorReading";
    public static final String ACTION_NEW_SENSOR_READING_RECORD = "si.uni_lj.fri.taskyapp.NewSensorReadingRecord";
    public static final String ACTION_KEEP_SENSING_ALIVE = "si.uni_lj.fri.taskyapp.KeepAliveAction";

    public static final String DATE_FORMAT_TO_SHOW_DAY = "EEE, MM yyyy ";
    public static final String DATE_FORMAT_TO_SHOW_FULL = "HH:mm:ss dd/MM/yyyy ";

    public static final String PREFS_LAST_SCREEN_STATE = "PREFS_LAST_SCREEN_STATE";
    public static final String PREFS_LAST_SCREEN_STATE_TIME = "PREFS_LAST_SCREEN_STATE_TIME";
}
