package si.uni_lj.fri.taskyapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import si.uni_lj.fri.taskyapp.data.DailyAggregatedData;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;

public class StatisticsActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsActivity";
    @Bind(R.id.daily_statistics_body_tv)
    TextView mDailyStatisticsBodyTv;

    @Bind(R.id.cardview_1_view_switcher)
    ViewSwitcher mCardViewSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ButterKnife.bind(this);

        new GetAndShowStatistics().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_label_tasks)
    public void labelTasks(View v){
        startActivity(new Intent(this, ListDataActivity.class));
    }

    class GetAndShowStatistics extends AsyncTask<Void, Void, DailyAggregatedData>{

        @Override
        protected DailyAggregatedData doInBackground(Void... params) {
            Calendar calendar = Calendar.getInstance();
            if(calendar.get(Calendar.HOUR_OF_DAY) < 2){
                calendar.set(Calendar.DAY_OF_YEAR, -1);
            }
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            List<SensorReadingRecord> sensorReadings = SensorReadingRecord.find(SensorReadingRecord.class,
                    "time_started_sensing > ?", new String[]{"" + calendar.getTimeInMillis()}, null, "time_started_sensing ASC", null);

            int sumLabels = 0, countLabels = 0;
            for (SensorReadingRecord srr : sensorReadings){
                if(srr.getLabel() != null && srr.getLabel() > 0){
                    sumLabels += srr.getLabel();
                    countLabels++;
                }
            }
            DailyAggregatedData dad = new DailyAggregatedData();
            dad.setAllReadings(sensorReadings.size());
            dad.setCountLabeled(countLabels);
            dad.setAverageLabel((sumLabels / (double) countLabels));
            Log.d(TAG, "Sum labels: " + sumLabels + ", countLabels = " + countLabels + ", average = " + dad.getAverageLabel());
            return dad;
        }

        @Override
        protected void onPostExecute(DailyAggregatedData dailyAggregatedData) {
            super.onPostExecute(dailyAggregatedData);

            String dailyBodyString = String.format(getString(R.string.daily_statistics_body), dailyAggregatedData.getAllReadings(), dailyAggregatedData.getCountLabeled());
            if(dailyAggregatedData.getCountLabeled() > 0){
                dailyBodyString += String.format(getString(R.string.daily_statistics_body_2), dailyAggregatedData.getAverageLabelTaskText(getBaseContext()));
            }
            mDailyStatisticsBodyTv.setText(Html.fromHtml(dailyBodyString));
            mCardViewSwitcher.setDisplayedChild(1);
        }
    }
}
