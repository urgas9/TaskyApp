package si.uni_lj.fri.taskyapp.sensor;

/**
 * Created by urgas9 on 10. 01. 2016.
 */
public class Constants {
    public static final String ACTION_NEW_SENSOR_READING = "taskyapp.NewSensorReading";
    public static final int APPROXIMATE_INTERVAL_MILLIS = 60000 * 30;//30000;
    public static final long SENSING_WINDOW_LENGTH_MILLIS = 30 * 1000;

    public static final String PREFS_LAST_LOC_LAT = "PREFS_LAST_LOC_LAT";
    public static final String PREFS_LAST_LOC_LNG = "PREFS_LAST_LOC_LNG";
    public static final String PREFS_LAST_LOC_ACCURACY = "PREFS_LAST_LOC_ACCURACY";

    public static final int LOCATION_ACCURACY_AT_LEAST = 200;
    public static final int LOCATION_MIN_DISTANCE_TO_LAST_LOC = 70;

    public static final String ACTION_KEEP_SENSING_ALIVE = "si.uni_lj.fri.taskyapp.KeepAliveAction";
}
