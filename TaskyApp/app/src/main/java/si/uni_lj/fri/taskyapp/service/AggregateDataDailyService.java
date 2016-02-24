package si.uni_lj.fri.taskyapp.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import si.uni_lj.fri.taskyapp.global.AppHelper;

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

    public AggregateDataDailyService() {
        super("AggregateDataDailyService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startService(Context context) {
        Intent intent = new Intent(context, AggregateDataDailyService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            AppHelper.aggregateDailyData();
        }
    }

}
