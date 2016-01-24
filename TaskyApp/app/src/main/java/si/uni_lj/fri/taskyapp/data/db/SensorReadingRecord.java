package si.uni_lj.fri.taskyapp.data.db;

import com.google.gson.Gson;
import com.orm.SugarRecord;

import si.uni_lj.fri.taskyapp.data.SensorReadingData;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class SensorReadingRecord extends SugarRecord {

    private String sensorJsonObject;

    public SensorReadingRecord(){

    }

    public SensorReadingRecord(String json){
        this.sensorJsonObject = json;
    }
    public SensorReadingRecord(SensorReadingData sensorReadingData){
        this.sensorJsonObject = new Gson().toJson(sensorReadingData);
    }

    public String getSensorJsonObject() {
        return sensorJsonObject;
    }

    public void setSensorJsonObject(String sensorJsonObject) {
        this.sensorJsonObject = sensorJsonObject;
    }

    @Override
    public String toString() {
        return "SensorReadingRecord{" +
                "sensorJsonObject='" + sensorJsonObject + '\'' +
                '}';
    }
}
