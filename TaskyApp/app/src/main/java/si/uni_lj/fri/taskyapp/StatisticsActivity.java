package si.uni_lj.fri.taskyapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.orm.SugarRecord;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
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
        Log.d(TAG, "onMayReady");

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
            mDailyChart.setDescription("Your tasks over past few days.");
            mDailyChart.invalidate();
        }
    }

    class GetAndShowHeatMapForLast2Days extends AsyncTask<Void, Void, ArrayList<LatLng>> {

        GoogleMap mMap;

        public GetAndShowHeatMapForLast2Days(GoogleMap mMap){
            super();
            this.mMap = mMap;
        }
        @Override
        protected ArrayList<LatLng> doInBackground(Void... params) {
            Calendar calendarFrom = AppHelper.getCalendarAtMidnight(-1);
            List<SensorReadingRecord> sensorReadings = SensorReadingRecord.find(SensorReadingRecord.class,
                    "time_started_sensing > ?", new String[]{"" + calendarFrom.getTimeInMillis()}, null, "time_started_sensing ASC", null);

            ArrayList<LatLng> resultList = new ArrayList<>();
            for(SensorReadingRecord srr : sensorReadings){
                resultList.add(new LatLng(srr.getLocationLat(), srr.getLocationLng()));
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(final ArrayList<LatLng> latLngs) {
            super.onPostExecute(latLngs);
            LatLngBounds.Builder bounds = new LatLngBounds.Builder();
            for (LatLng latLng : latLngs) {
                if(latLng.latitude != 0 && latLng.longitude != 0) {
                    bounds.include(latLng);
                    //mMap.addMarker(new MarkerOptions().position(latLng));
                }
            }

            Log.d("LatLngBounds", "Number: " + latLngs.size());

            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 120));

            TileProvider mProvider = new HeatmapTileProvider.Builder()
                    .data(latLngs)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
            mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    Intent mapIntent = new Intent(StatisticsActivity.this, GoogleMapFullScreenActivity.class);
                    mapIntent.putParcelableArrayListExtra("latLngs", latLngs);
                    startActivity(mapIntent);
                }
            });
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

        int i = 0;
        for(DailyAggregatedData dad : dailyDataList){
            xVals.add(dad.getStringDay());
            yVals1.add(new BarEntry(new float[]{dad.getCountLabeled(), dad.getAllReadings()-dad.getCountLabeled()}, i));
            i++;
        }
        BarDataSet set1 = new BarDataSet(yVals1, "");
        // set1.setColors(ColorTemplate.createColors(getApplicationContext(),
        // ColorTemplate.FRESH_COLORS));

        set1.setColors(new int[]{ContextCompat.getColor(this, R.color.accent), ContextCompat.getColor(this, R.color.primary_light)});
        set1.setStackLabels(new String[]{"# labeled tasks", "# not labeled tasks"});

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

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