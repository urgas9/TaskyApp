package si.uni_lj.fri.taskyapp.data.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by urgas9 on 20-Feb-16, OpenHours.com
 */
public class PostDataResponse {

    private boolean success;
    @SerializedName("confirmed_ids")
    private List<Long> confirmedIds;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Long> getConfirmedIds() {
        return confirmedIds;
    }

    public void setConfirmedIds(List<Long> confirmedIds) {
        this.confirmedIds = confirmedIds;
    }

    @Override
    public String toString() {
        return "PostDataResponse{" +
                "success=" + success +
                ", confirmedIds=" + confirmedIds +
                '}';
    }
}
