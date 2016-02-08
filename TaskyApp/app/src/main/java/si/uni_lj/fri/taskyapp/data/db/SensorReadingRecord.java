package si.uni_lj.fri.taskyapp.data.db;

import com.google.gson.Gson;
import com.orm.SugarRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import si.uni_lj.fri.taskyapp.data.SensorReadingData;
import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 24. 01. 2016.
 *
 */
public class SensorReadingRecord extends SugarRecord {

    private String sensorJsonObject;
    private boolean startedByUser;
    private Integer label;
    private long timeSaved;
    private String daySensed;

    public SensorReadingRecord(){

    }

    public SensorReadingRecord(SensorReadingData sensorReadingData, boolean startedByUser, int label){
        this.sensorJsonObject = new Gson().toJson(sensorReadingData);
        this.startedByUser = startedByUser;
        this.label = label;
        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT_TO_SHOW_DAY, Locale.ENGLISH);
        this.daySensed = format.format(new Date(sensorReadingData.getTimestampStarted()));
        this.timeSaved = System.currentTimeMillis();
    }

    public String getSensorJsonObject() {
        return sensorJsonObject;
    }

    public void setSensorJsonObject(String sensorJsonObject) {
        this.sensorJsonObject = sensorJsonObject;
    }

    public boolean isStartedByUser() {
        return startedByUser;
    }

    public void setStartedByUser(boolean startedByUser) {
        this.startedByUser = startedByUser;
    }

    public Integer getLabel() {
        return label;
    }

    public void setLabel(Integer label) {
        this.label = label;
    }

    public long getTimeSaved() {
        return timeSaved;
    }

    public void setTimeSaved(long timeSaved) {
        this.timeSaved = timeSaved;
    }

    public String getDaySensed() {
        return daySensed;
    }

    public void setDaySensed(String daySensed) {
        this.daySensed = daySensed;
    }

    @Override
    public String toString() {
        return "SensorReadingRecord{" +
                "sensorJsonObject='" + sensorJsonObject + '\'' +
                ", startedByUser=" + startedByUser +
                ", label=" + label +
                ", timeSaved=" + timeSaved +
                ", daySensed='" + daySensed + '\'' +
                '}';
    }
}
