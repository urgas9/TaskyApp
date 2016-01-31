package si.uni_lj.fri.taskyapp;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import si.uni_lj.fri.taskyapp.broadcast_receivers.NewSensorReadingReceiver;
import si.uni_lj.fri.taskyapp.data.SensorReadingData;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.sensor.Constants;


// Activity recognition android: http://tutsberry.com/activity-recognition-implementation-on-android/
public class ListDataActivity extends AppCompatActivity {

    private static final String TAG = "ListResultsActivity";
    BroadcastReceiver newSensorReadingReceiver;

    @Bind(R.id.app_status_tv)
    TextView mStatusTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_list_data);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStatusTextView.setMovementMethod(new ScrollingMovementMethod());
        mStatusTextView.setText("Launching ListResultsActivity.");

        newSensorReadingReceiver = new NewSensorReadingReceiver(mStatusTextView);

        //Filter the Intent and register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_NEW_SENSOR_READING);
        registerReceiver(newSensorReadingReceiver, filter);

        new ReadAllSensorRecords().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Disconnect and detach the receiver
        unregisterReceiver(newSensorReadingReceiver);
    }

    class ReadAllSensorRecords extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... params) {
            List<SensorReadingRecord> sensorReadings = SensorReadingRecord.listAll(SensorReadingRecord.class);
            StringBuilder sb = new StringBuilder();
            Gson gson = new Gson();
            SensorReadingData rec;
            sb.append("All records found: ").append(sensorReadings.size()).append("\n");
            for(SensorReadingRecord srr : sensorReadings){
                rec = gson.fromJson(srr.getSensorJsonObject(), SensorReadingData.class);
                String time = new java.text.SimpleDateFormat("dd/MM HH:mm:ss").format(new Date(rec.getTimestampStarted()));
                sb.append(time).append(": ").append(rec.getActivityData());
                sb.append("\n");
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mStatusTextView.setText(s);
        }
    }

}
