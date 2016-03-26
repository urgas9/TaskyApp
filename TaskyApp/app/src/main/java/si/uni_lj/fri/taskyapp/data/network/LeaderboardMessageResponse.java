package si.uni_lj.fri.taskyapp.data.network;

import com.google.gson.annotations.SerializedName;

/**
 * Created by urgas9 on 26.3.16, OpenHours.com
 */
public class LeaderboardMessageResponse {

    private boolean success;
    private String message;
    @SerializedName("hide_message")
    private boolean hideMessage;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isHideMessage() {
        return hideMessage;
    }

    public void setHideMessage(boolean hideMessage) {
        this.hideMessage = hideMessage;
    }

    @Override
    public String toString() {
        return "LeaderboardMessageResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", hideMessage=" + hideMessage +
                '}';
    }
}
