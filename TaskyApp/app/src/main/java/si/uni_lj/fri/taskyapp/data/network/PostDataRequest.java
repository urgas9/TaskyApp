package si.uni_lj.fri.taskyapp.data.network;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import si.uni_lj.fri.taskyapp.data.Auth;
import si.uni_lj.fri.taskyapp.data.SensorReadingData;

/**
 * Created by urgas9 on 20-Feb-16, OpenHours.com
 */
public class PostDataRequest {

    private Auth auth;
    @SerializedName("data")
    private List<SensorReadingData> dataList;

    public PostDataRequest(Context ctx, List<SensorReadingData> dataList) {
        super();
        this.auth = new Auth(ctx);
        this.dataList = dataList;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public List<SensorReadingData> getDataList() {
        return dataList;
    }

    public void setDataList(List<SensorReadingData> dataList) {
        this.dataList = dataList;
    }

    @Override
    public String toString() {
        return "PostDataRequest{" +
                "auth=" + auth +
                ", dataList=" + dataList +
                '}';
    }
}
