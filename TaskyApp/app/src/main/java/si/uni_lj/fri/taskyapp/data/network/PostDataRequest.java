package si.uni_lj.fri.taskyapp.data.network;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import si.uni_lj.fri.taskyapp.data.SensorReadingData;

/**
 * Created by urgas9 on 20-Feb-16, OpenHours.com
 */
public class PostDataRequest extends AuthRequest {

    @SerializedName("data")
    private List<SensorReadingData> dataList;

    public PostDataRequest(Context ctx, List<SensorReadingData> dataList) {
        super(ctx);
        this.dataList = dataList;
    }

    public List<SensorReadingData> getDataList() {
        return dataList;
    }

    public void setDataList(List<SensorReadingData> datsaList) {
        this.dataList = dataList;
    }

    @Override
    public String toString() {
        return "PostDataRequest{" +
                "auth=" + getAuth() +
                ", dataList=" + dataList +
                '}';
    }
}
