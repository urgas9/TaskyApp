package si.uni_lj.fri.taskyapp.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

import si.uni_lj.fri.taskyapp.sensor.Constants;

/**
 * Created by urgas9 on 28/02/2016, OpenHours.com
 */
public class MarkerDataHolder implements Parcelable{
    public String time;
    public int label;
    public LatLng latLng;
    public long dbRecordId;

    public MarkerDataHolder(long dbRecordId, long timeMillis, int label, double lat, double lng){
        super();
        this.dbRecordId = dbRecordId;
        setTime(timeMillis);
        this.label = label;
        this.latLng = new LatLng(lat, lng);;
    }
    protected MarkerDataHolder(Parcel in) {
        time = in.readString();
        label = in.readInt();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        dbRecordId = in.readLong();
    }

    public static final Creator<MarkerDataHolder> CREATOR = new Creator<MarkerDataHolder>() {
        @Override
        public MarkerDataHolder createFromParcel(Parcel in) {
            return new MarkerDataHolder(in);
        }

        @Override
        public MarkerDataHolder[] newArray(int size) {
            return new MarkerDataHolder[size];
        }
    };

    public void setTime(long timeMillis){
        SimpleDateFormat formatFullDate = new SimpleDateFormat(Constants.DATE_FORMAT_TO_SHOW_FULL);
        time = formatFullDate.format(new Date(timeMillis));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(time);
        dest.writeInt(label);
        dest.writeParcelable(latLng, flags);
        dest.writeLong(dbRecordId);
    }
}
