package si.uni_lj.fri.taskyapp.data;

/**
 * Created by urgas9 on 21-Aug-16, OpenHours.com
 */
public class AngelSensorData {

    private Boolean isConnected;
    private Float temperature;
    private Integer hearRate;

    public AngelSensorData() {
        this.isConnected = Boolean.FALSE;
    }

    public Boolean getConnected() {
        return isConnected;
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Integer getHearRate() {
        return hearRate;
    }

    public void setHearRate(Integer hearRate) {
        this.hearRate = hearRate;
    }

    @Override
    public String toString() {
        return "AngelSensorData{" +
                "isConnected=" + isConnected +
                ", temperature=" + temperature +
                ", hearRate=" + hearRate +
                '}';
    }

    public boolean waitForData() {
        return isConnected && (temperature == null || hearRate == null);
    }
}
