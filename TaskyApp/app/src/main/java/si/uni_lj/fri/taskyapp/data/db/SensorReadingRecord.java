package si.uni_lj.fri.taskyapp.data.db;

import com.google.gson.Gson;
import com.orm.SugarRecord;

import si.uni_lj.fri.taskyapp.data.SensorReadingData;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class SensorReadingRecord extends SugarRecord {

    private String sensorJsonObject;
    private boolean startedByUser;
    private Integer label;

    public SensorReadingRecord(){

    }

    public SensorReadingRecord(String json, boolean startedByUser, int label){
        this.sensorJsonObject = json;
        this.startedByUser = startedByUser;
        this.label = label;
    }
    public SensorReadingRecord(SensorReadingData sensorReadingData, boolean startedByUser, int label){
        this(new Gson().toJson(sensorReadingData), startedByUser, label);
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

    @Override
    public String toString() {
        return "SensorReadingRecord{" +
                "sensorJsonObject='" + sensorJsonObject + '\'' +
                ", startedByUser=" + startedByUser +
                ", label=" + label +
                '}';
    }
}
