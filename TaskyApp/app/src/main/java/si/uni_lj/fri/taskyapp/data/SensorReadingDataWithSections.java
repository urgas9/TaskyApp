package si.uni_lj.fri.taskyapp.data;

import java.util.ArrayList;

/**
 * Created by urgas9 on 7. 02. 2016.
 */
public class SensorReadingDataWithSections {
    private int numSections;
    private ArrayList<SensorReadingData> dataList;

    public SensorReadingDataWithSections(int numSections, ArrayList<SensorReadingData> data){
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

    public ArrayList<SensorReadingData> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<SensorReadingData> dataList) {
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
