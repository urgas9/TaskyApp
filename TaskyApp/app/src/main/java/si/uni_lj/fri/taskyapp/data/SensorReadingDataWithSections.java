package si.uni_lj.fri.taskyapp.data;

import java.util.ArrayList;

import si.uni_lj.fri.taskyapp.data.db.SensorReadingRecord;

/**
 * Created by urgas9 on 7. 02. 2016.
 */
public class SensorReadingDataWithSections {
    private int numSections;
    private ArrayList<SensorReadingRecord> dataList;

    public SensorReadingDataWithSections(int numSections, ArrayList<SensorReadingRecord> data){
        super();
        this.numSections = numSections;
        this.dataList = data;
    }

    public int getNumSections() {
        return numSections;
    }

    public void setNumSections(int numSections) {
        this.numSections = numSections;
    }

    public ArrayList<SensorReadingRecord> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<SensorReadingRecord> dataList) {
        this.dataList = dataList;
    }

    @Override
    public String toString() {
        return "SensorReadingDataWithSections{" +
                "numSections=" + numSections +
                ", dataList=" + dataList +
                '}';
    }
}
