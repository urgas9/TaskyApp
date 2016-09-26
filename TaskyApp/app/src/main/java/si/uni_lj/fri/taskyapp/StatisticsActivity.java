/*
 * Copyright (c) 2016, University of Ljubljana, Slovenia
 *
 * Gasper Urh, gu7668@student.uni-lj.si
 *
 * This project was developed as part of the paper submitted for the UbitTention workshop paper (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package si.uni_lj.fri.taskyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.orm.SugarRecord;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import si.uni_lj.fri.taskyapp.data.MarkerDataHolder;
import si.uni_lj.fri.taskyapp.data.db.DailyAggregatedData;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.data.network.AuthRequest;
import si.uni_lj.fri.taskyapp.data.network.LeaderboardMessageResponse;
import si.uni_lj.fri.taskyapp.global.AppHelper;
import si.uni_lj.fri.taskyapp.networking.ApiUrls;
import si.uni_lj.fri.taskyapp.networking.ConnectionHelper;
import si.uni_lj.fri.taskyapp.networking.ConnectionResponse;
import si.uni_lj.fri.taskyapp.sensor.Constants;

public class StatisticsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "StatisticsActivity";
    @Bind(R.id.daily_statistics_body_tv)
    TextView mDailyStatisticsBodyTv;
    @Bind(R.id.leaderboard_msg_tv)
    TextView mLeaderboardMsgTv;
    @Bind(R.id.leaderboard_title_tv)
    TextView mLeaderboardTitleTv;

    @Bind(R.id.cardview_1_view_switcher)
    ViewSwitcher mCard1ViewSwitcher;
    @Bind(R.id.cardview_2_view_switcher)
    ViewSwitcher mCard2ViewSwitcher;
    @Bind(R.id.cardview_3_view_switcher)
    ViewSwitcher mCard3ViewSwitcher;
    @Bind(R.id.cardview_4_view_switcher)
    ViewSwitcher mCard4ViewSwitcher;
    @Bind(R.id.chart1)
    BarChart mDailyChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ButterKnife.bind(this);

        mDailyChart.animateY(2500);
        new GetAndShowStatistics().execute();

        new GetAndShowLeaderboardMsg().execute();

        new GetAndShowGraphStatistics().execute();

        new GetAndShowHeatMapForLast2Days().execute();
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
    public void labelTasks(View v) {
        startActivity(new Intent(this, ListDataActivity.class));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMayReady");

    }

    private DailyAggregatedData getDailyAggregatedData(int relativeDay, boolean showFromPreviousDayIfTill3) {
        Calendar calendarFrom = AppHelper.getCalendarAtMidnight(relativeDay);
        if (showFromPreviousDayIfTill3 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 3) {
            calendarFrom = AppHelper.getCalendarAtMidnight(relativeDay - 1);
        }
        long millisFrom = calendarFrom.getTimeInMillis();
        Calendar calendarTo = AppHelper.getCalendarAtMidnight(relativeDay + 1);
        long millisTo = calendarTo.getTimeInMillis();
        if (showFromPreviousDayIfTill3) {
            millisTo = Calendar.getInstance().getTimeInMillis();
        }

        List<SensorReadingRecord> sensorReadings = SensorReadingRecord.find(SensorReadingRecord.class,
                "time_started_sensing > ? AND time_started_sensing < ?", new String[]{"" + millisFrom, "" + millisTo}, null, "time_started_sensing ASC", null);

        int sumLabels = 0, countLabels = 0;
        for (SensorReadingRecord srr : sensorReadings) {
            if (srr.getLabel() != null && srr.getLabel() > 0) {
                sumLabels += srr.getLabel();
                countLabels++;
            }
        }
        DailyAggregatedData dad = new DailyAggregatedData();
        dad.setAllReadings(sensorReadings.size());
        dad.setDayOfYear(calendarFrom.get(Calendar.DAY_OF_YEAR));
        dad.setCountLabeled(countLabels);
        dad.setAverageLabel((sumLabels / (double) countLabels));
        return dad;
    }

    private BarData getDailyCharBarData(List<DailyAggregatedData> dailyDataList) {
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<BarEntry> yVals1 = new ArrayList<>();

        int i = 0;
        for (DailyAggregatedData dad : dailyDataList) {
            xVals.add(dad.getStringDay());
            yVals1.add(new BarEntry(new float[]{dad.getCountLabeled(), dad.getAllReadings() - dad.getCountLabeled()}, i));
            i++;
        }
        BarDataSet set1 = new BarDataSet(yVals1, "");
        // set1.setColors(ColorTemplate.createColors(getApplicationContext(),
        // ColorTemplate.FRESH_COLORS));

        set1.setColors(new int[]{ContextCompat.getColor(this, R.color.accent), ContextCompat.getColor(this, R.color.primary_light)});
        set1.setStackLabels(new String[]{getString(R.string.legend_num_labeled), getString(R.string.legend_num_non_labeled)});

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueFormatter(new MyValueFormatter());
        return data;
    }

    class GetAndShowLeaderboardMsg extends AsyncTask<Void, Void, ConnectionResponse<LeaderboardMessageResponse>> {

        @Override
        protected ConnectionResponse<LeaderboardMessageResponse> doInBackground(Void... params) {

            AuthRequest request = new AuthRequest(getApplicationContext());
            return ConnectionHelper.postHttpDataCustomUrl(
                    getApplicationContext(),
                    ApiUrls.getApiCall(getApplicationContext(), ApiUrls.POST_LEADERBOARD_MSG),
                    request,
                    LeaderboardMessageResponse.class);
        }

        @Override
        protected void onPostExecute(ConnectionResponse<LeaderboardMessageResponse> leaderboardMessageResponseConnectionResponse) {
            super.onPostExecute(leaderboardMessageResponseConnectionResponse);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String textViewMessage = "Ooops! It appears there are some issues with your network.";
            String leaderBoardTitle = getString(R.string.leaderboard);
            if (leaderboardMessageResponseConnectionResponse.isSuccess() && leaderboardMessageResponseConnectionResponse.getContent().isSuccess()) {
                Log.d(TAG, "Leaderboard message successfully received.");
                LeaderboardMessageResponse responseContent = leaderboardMessageResponseConnectionResponse.getContent();
                textViewMessage = responseContent.getMessage();
                if (responseContent.getTitle() != null) {
                    leaderBoardTitle = responseContent.getTitle();
                }
                if (leaderboardMessageResponseConnectionResponse.getContent().isHideMessage()) {
                    mCard1ViewSwitcher.setVisibility(View.GONE);
                    prefs.edit().putBoolean(Constants.PREFS_SHOW_LEADERBOARD_MSG, false).apply();

                } else {
                    mCard1ViewSwitcher.setVisibility(View.VISIBLE);
                    prefs.edit().putBoolean(Constants.PREFS_SHOW_LEADERBOARD_MSG, true).apply();
                }
            } else {
                Log.d(TAG, "Cannot get leaderboard message");
                if (!prefs.getBoolean(Constants.PREFS_SHOW_LEADERBOARD_MSG, true)) {
                    mCard1ViewSwitcher.setVisibility(View.GONE);
                }
            }
            mLeaderboardTitleTv.setText(leaderBoardTitle);
            mLeaderboardMsgTv.setText(Html.fromHtml(textViewMessage));
            mCard1ViewSwitcher.setDisplayedChild(1);
        }
    }

    class GetAndShowStatistics extends AsyncTask<Void, Void, DailyAggregatedData> {

        @Override
        protected DailyAggregatedData doInBackground(Void... params) {
            return getDailyAggregatedData(0, true);
        }

        @Override
        protected void onPostExecute(DailyAggregatedData dailyAggregatedData) {
            super.onPostExecute(dailyAggregatedData);

            StringBuilder sb = new StringBuilder("<html>");
            sb.append(getString(R.string.daily_statistics_body_1));
            sb.append(" <b>");
            sb.append(dailyAggregatedData.getAllReadings());
            sb.append("</b> ");
            sb.append(getString(R.string.daily_statistics_body_2));
            sb.append(" <b>");
            sb.append(dailyAggregatedData.getCountLabeled());
            sb.append(".</b> ");
            if (dailyAggregatedData.getCountLabeled() > 0) {
                sb.append(getString(R.string.daily_statistics_body_3));
                sb.append(" <b>");
                sb.append(dailyAggregatedData.getAverageLabelTaskText(getBaseContext()));
                sb.append("</b>.");
            }
            sb.append("</html>");
            Log.d(TAG, "dailyBodyString: " + sb.toString());

            if (dailyAggregatedData.getAllReadings() == dailyAggregatedData.getCountLabeled()) {
                View labelMoreTasksBtn = findViewById(R.id.btn_label_tasks);
                if (labelMoreTasksBtn != null) {
                    labelMoreTasksBtn.setVisibility(View.GONE);
                }
            }
            mDailyStatisticsBodyTv.setText(Html.fromHtml(sb.toString()));
            mCard2ViewSwitcher.setDisplayedChild(1);
        }
    }

    class GetAndShowGraphStatistics extends AsyncTask<Void, Void, BarData> {

        @Override
        protected BarData doInBackground(Void... params) {
            List<DailyAggregatedData> resultList = SugarRecord.listAll(DailyAggregatedData.class);
            resultList.add(getDailyAggregatedData(-1, false));
            resultList.add(getDailyAggregatedData(0, false));

            return getDailyCharBarData(resultList);
        }

        @Override
        protected void onPostExecute(BarData data) {
            super.onPostExecute(data);

            mCard3ViewSwitcher.setDisplayedChild(1);
            mDailyChart.setData(data);
            mDailyChart.setMaxVisibleValueCount(7);
            mDailyChart.setDescription("Your tasks over past few days.");
            mDailyChart.invalidate();
        }
    }

    class GetAndShowHeatMapForLast2Days extends AsyncTask<Void, Void, ArrayList<MarkerDataHolder>> {

        @Override
        protected ArrayList<MarkerDataHolder> doInBackground(Void... params) {
            List<SensorReadingRecord> sensorReadings = AppHelper.getSensorRecordsOfLastTwoDays();

            ArrayList<MarkerDataHolder> resultList = new ArrayList<>();
            for (SensorReadingRecord srr : sensorReadings) {
                if (srr.getLocationLat() != 0.0 && srr.getLocationLng() != 0.0) {
                    resultList.add(srr.getMarkerDataHolder());
                }
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(final ArrayList<MarkerDataHolder> resultArray) {
            super.onPostExecute(resultArray);
            if (resultArray == null || resultArray.isEmpty()) {
                return;
            }

            FullScreenMapFragment fragment = FullScreenMapFragment.newInstance(FullScreenMapFragment.VIEW_HEATMAP, resultArray, false);
            getSupportFragmentManager().beginTransaction().replace(R.id.map_content_frame, fragment).commit();
            mCard4ViewSwitcher.setDisplayedChild(1);

            findViewById(R.id.map_click_interceptor).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapIntent = new Intent(StatisticsActivity.this, GoogleMapFullScreenActivity.class);
                    mapIntent.putParcelableArrayListExtra("markerDataArray", resultArray);
                    startActivity(mapIntent);
                }
            });
        }
    }

    class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,###,##0");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }
    }

}
