package si.uni_lj.fri.taskyapp.data;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 7. 02. 2016.
 */
public class ListDataAdapterSectionItem {

    private String textDate;
    private ArrayList<SensorReadingData> dataList;

    public ListDataAdapterSectionItem(long timestamp, ArrayList<SensorReadingData> list) {
        super();
        Date date = new Date(timestamp);
        Format format = new SimpleDateFormat(Constants.DATE_FORMAT_TO_SHOW_DAY, Locale.ENGLISH);
        this.textDate = format.format(date);
        this.dataList = list;
    }


    @Override
    public String toString() {
        return "ListDataAdapterSection{" +
                "textDate='" + textDate + '\'' +
                ", dataList=" + dataList +
                '}';
    }

    public String getTextDate() {
        return textDate;
    }

    public void setTextDate(String textDate) {
        this.textDate = textDate;
    }

    public ArrayList<SensorReadingData> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<SensorReadingData> dataList) {
        this.dataList = dataList;
    }
}
