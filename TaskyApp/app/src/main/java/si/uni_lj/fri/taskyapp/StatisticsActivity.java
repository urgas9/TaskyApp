package si.uni_lj.fri.taskyapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.orm.SugarRecord;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import si.uni_lj.fri.taskyapp.data.db.DailyAggregatedData;
import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;
import si.uni_lj.fri.taskyapp.global.AppHelper;

public class StatisticsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "StatisticsActivity";
    @Bind(R.id.daily_statistics_body_tv)
    TextView mDailyStatisticsBodyTv;

    @Bind(R.id.cardview_1_view_switcher)
    ViewSwitcher mCard1ViewSwitcher;
    @Bind(R.id.cardview_2_view_switcher)
    ViewSwitcher mCard2ViewSwitcher;
    @Bind(R.id.chart1)
    BarChart mDailyChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ButterKnife.bind(this);

        mDailyChart.animateY(2500);
        new GetAndShowStatistics().execute();

        new GetAndShowGraphStatistics().execute();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        new GetAndShowHeatMapForLast2Days(googleMap).execute();
    }

    class GetAndShowStatistics extends AsyncTask<Void, Void, DailyAggregatedData>{

        @Override
        protected DailyAggregatedData doInBackground(Void... params) {
            return getDailyAggregatedData(0, true);
        }

        @Override
        protected void onPostExecute(DailyAggregatedData dailyAggregatedData) {
            super.onPostExecute(dailyAggregatedData);

            String dailyBodyString = String.format(getString(R.string.daily_statistics_body), dailyAggregatedData.getAllReadings(), dailyAggregatedData.getCountLabeled());
            if(dailyAggregatedData.getCountLabeled() > 0){
                dailyBodyString += String.format(getString(R.string.daily_statistics_body_2), dailyAggregatedData.getAverageLabelTaskText(getBaseContext()));
            }
            mDailyStatisticsBodyTv.setText(Html.fromHtml(dailyBodyString));
            mCard1ViewSwitcher.setDisplayedChild(1);
        }
    }

    class GetAndShowGraphStatistics extends AsyncTask<Void,Void,BarData>{

        @Override
        protected BarData  doInBackground(Void... params) {
            List<DailyAggregatedData> resultList = SugarRecord.listAll(DailyAggregatedData.class);
            resultList.add(getDailyAggregatedData(-1, false));
            resultList.add(getDailyAggregatedData(0, false));

            return getDailyCharBarData(resultList);
        }

        @Override
        protected void onPostExecute(BarData data) {
            super.onPostExecute(data);


            mCard2ViewSwitcher.setDisplayedChild(1);
            mDailyChart.setData(data);
            mDailyChart.setMaxVisibleValueCount(2);
            mDailyChart.invalidate();
        }
    }

    class GetAndShowHeatMapForLast2Days extends AsyncTask<Void, Void, List<LatLng>> {
        GoogleMap mMap;
        public GetAndShowHeatMapForLast2Days(GoogleMap mMap){
            this.mMap = mMap;
        }
        @Override
        protected List<LatLng> doInBackground(Void... params) {
            Calendar calendarFrom = AppHelper.getCalendarAtMidnight(-1);
            List<SensorReadingRecord> sensorReadings = SensorReadingRecord.find(SensorReadingRecord.class,
                    "time_started_sensing > ?", new String[]{"" + calendarFrom.getTimeInMillis()}, null, "time_started_sensing ASC", null);

            List<LatLng> resultList = new LinkedList<>();
            for(SensorReadingRecord srr : sensorReadings){
                resultList.add(new LatLng(srr.getLocationLat(), srr.getLocationLng()));
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(List<LatLng> latLngs) {
            super.onPostExecute(latLngs);
            TileProvider mProvider = new HeatmapTileProvider.Builder()
                    .data(latLngs)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
            mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
    }

    private DailyAggregatedData getDailyAggregatedData(int relativeDay, boolean showFromPreviousDayIfTill3){
        Calendar calendarFrom = AppHelper.getCalendarAtMidnight(relativeDay);
        if(showFromPreviousDayIfTill3 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 3){
            calendarFrom = AppHelper.getCalendarAtMidnight(relativeDay - 1);
        }
        long millisFrom = calendarFrom.getTimeInMillis();
        Calendar calendarTo = AppHelper.getCalendarAtMidnight(relativeDay + 1);
        long millisTo = calendarTo.getTimeInMillis();
        if(showFromPreviousDayIfTill3){
            millisTo = Calendar.getInstance().getTimeInMillis();
        }

        List<SensorReadingRecord> sensorReadings = SensorReadingRecord.find(SensorReadingRecord.class,
                "time_started_sensing > ? AND time_started_sensing < ?", new String[]{"" + millisFrom, "" + millisTo}, null, "time_started_sensing ASC", null);

        int sumLabels = 0, countLabels = 0;
        for (SensorReadingRecord srr : sensorReadings){
            if(srr.getLabel() != null && srr.getLabel() > 0){
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

    private BarData getDailyCharBarData(List<DailyAggregatedData> dailyDataList){
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        ArrayList<BarEntry> yVals2 = new ArrayList<>();
        ArrayList<BarEntry> yVals3 = new ArrayList<>();

        int i = 0;
        for(DailyAggregatedData dad : dailyDataList){
            xVals.add(dad.getStringDay());
            yVals1.add(new BarEntry(new float[]{dad.getAllReadings()}, i));
            yVals2.add(new BarEntry(new float[]{dad.getCountLabeled()}, i));
            yVals3.add(new BarEntry(new float[]{(float)dad.getAverageLabel()}, i));
            i++;
        }
        BarDataSet set1 = new BarDataSet(yVals1, "# all tasks");
        // set1.setColors(ColorTemplate.createColors(getApplicationContext(),
        // ColorTemplate.FRESH_COLORS));
        set1.setColor(Color.rgb(104, 241, 175));
        BarDataSet set2 = new BarDataSet(yVals2, "# labeled tasks");
        set2.setColor(Color.rgb(164, 228, 251));
        BarDataSet set3 = new BarDataSet(yVals3, "Average label");
        set3.setColor(Color.rgb(242, 247, 158));

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);
        dataSets.add(set2);
        dataSets.add(set3);

        BarData data = new BarData(xVals, dataSets);
        data.setValueFormatter(new MyValueFormatter());
        return data;
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
