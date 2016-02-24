package si.uni_lj.fri.taskyapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.util.Calendar;
import java.util.List;

import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AggregateDataDailyService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String TAG = "AggregateDataDaily";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "si.uni_lj.fri.taskyapp.service.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "si.uni_lj.fri.taskyapp.service.extra.PARAM2";

    public AggregateDataDailyService() {
        super("AggregateDataDailyService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startService(Context context, String param1, String param2) {
        Intent intent = new Intent(context, AggregateDataDailyService.class);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.DAY_OF_YEAR, -1);

            List<SensorReadingRecord> sensorReadings = SensorReadingRecord.find(SensorReadingRecord.class,
                    "time_started_sensing < ?", new String[]{"" + calendar.getTimeInMillis()}, null, "time_started_sensing ASC", null);
        }
    }

}
