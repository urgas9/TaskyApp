package si.uni_lj.fri.taskyapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import si.uni_lj.fri.taskyapp.data.SensorReadingData;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.data.network.PostDataRequest;
import si.uni_lj.fri.taskyapp.data.network.PostDataResponse;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.networking.ApiUrls;
import si.uni_lj.fri.taskyapp.networking.ConnectionHelper;
import si.uni_lj.fri.taskyapp.networking.ConnectionResponse;
import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 20-Feb-16, OpenHours.com
 */
public class SendDataToServerService extends IntentService {
    private static final String TAG = "SendDataToServerService";
    private SharedPreferences mPrefs;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SendDataToServerService(String name) {
        super(name);
    }

    public SendDataToServerService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Handling intent to send data to server.");

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (!AppHelper.isConnectedToWifi(getBaseContext())) {
            Log.d(TAG, "Not connected to Wifi! Back off.");
            return;
        }

        long lastTimestamp = mPrefs.getLong(Constants.PREFS_LAST_TIME_SENT_TO_SERVER, 0);
        //TODO: Uncomment
        /*if ((lastTimestamp + Constants.MAX_INTERVAL_BETWEEN_TWO_SERVER_POSTS) > System.currentTimeMillis()) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            Log.d(TAG, "Won't post to server as the task executed " + format.format(new Date(System.currentTimeMillis() - lastTimestamp)) + " ago.");
            return;
        }*/

        AppHelper.aggregateDailyData();

        mPrefs.edit().putLong(Constants.PREFS_LAST_TIME_SENT_TO_SERVER, System.currentTimeMillis()).apply();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        List<SensorReadingRecord> sensorReadings = SensorReadingRecord.find(SensorReadingRecord.class,
                "time_started_sensing <= ?", new String[]{"" + calendar.getTimeInMillis()}, null, "time_started_sensing ASC", null);

        List<SensorReadingData> sensorReadingDataList = new LinkedList<>();

        Gson gson = new Gson();
        int countDeleted = 0;
        for (SensorReadingRecord srr : sensorReadings) {
            if (srr.getLabel() == null || srr.getLabel() <= 0) {
                //TODO: Remove after testing
                //countDeleted += srr.delete();
                continue;
            }
            SensorReadingData srd = gson.fromJson(srr.getSensorJsonObject(), SensorReadingData.class);
            srd.setLabel(srr.getLabel());
            srd.setDbRecordId(srr.getId());
            sensorReadingDataList.add(srd);
        }
        // Post data to server using exponential backoff
        ConnectionResponse<PostDataResponse> result;
        final int MAX_TRIES = 3;
        int count = 0, backoffMillis = 1000;
        do {
            Log.d(TAG, "Trying to post data to server.");
            result = ConnectionHelper.postHttpDataCustomUrl(getBaseContext(),
                    ApiUrls.getApiCall(getBaseContext(), ApiUrls.POST_RESULTS),
                    new PostDataRequest(getBaseContext(), sensorReadingDataList),
                    PostDataResponse.class);

            if (result.isSuccess()) {
                break;
            } else {
                Log.d(TAG, "Posting data to server did not succeed, retrying in " + backoffMillis);
                try {
                    Thread.sleep(backoffMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                backoffMillis *= 2;
                count++;
            }
        } while (count < MAX_TRIES);

        if (result.isSuccess()) {
            if(result.getContent().isSuccess()) {
                Log.d(TAG, "Data posted to server successfully");
                for (Long confirmedId : result.getContent().getConfirmedIds()) {
                    //TODO: Change this to delete!
                    SensorReadingRecord srr = SensorReadingRecord.findById(SensorReadingRecord.class, confirmedId);
                    if(srr != null) {
                        Log.d(TAG, "DELETE: " + srr.getAddress());
                    }
                    else{
                        Log.d(TAG, "Not found any items for: " + confirmedId);
                    }
                }
            }
            else{
                Log.e(TAG, "Post was made to server, but server returned false.");
            }

        } else {
            Log.e(TAG, "Cannot post data to server, code: " + result.getResponseCode());
        }

    }

}