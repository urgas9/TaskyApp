package si.uni_lj.fri.taskyapp.data;

import android.content.Context;
import android.util.Log;

import si.uni_lj.fri.taskyapp.R;

/**
 * Created by urgas9 on 21-Feb-16, OpenHours.com
 */
public class DailyAggregatedData {
    private long allReadings;
    private long countLabeled;
    private double averageLabel;

    public long getAllReadings() {
        return allReadings;
    }

    public void setAllReadings(long allReadings) {
        this.allReadings = allReadings;
    }

    public long getCountLabeled() {
        return countLabeled;
    }

    public void setCountLabeled(long countLabeled) {
        this.countLabeled = countLabeled;
    }

    public double getAverageLabel() {
        return averageLabel;
    }

    public void setAverageLabel(double averageLabel) {
        this.averageLabel = averageLabel;
    }

    public String getAverageLabelTaskText(Context ctx){
        String[] complexitiesArray = ctx.getResources().getStringArray(R.array.task_complexities_array);

        int base = (int) averageLabel;
        double decimal = averageLabel - base;

        Log.d("DailyAggregatedData", "Base: " + base + ", decimal part: " + decimal);
        if(decimal > 0.5){
            if(base == 4){
                return "almost " + complexitiesArray[base];
            }
            return "closer to " + complexitiesArray[base+1] + " than " + complexitiesArray[base];
        }
        else{
            if(base == 1){
                return "close to " + complexitiesArray[base];
            }
            return "closer to " + complexitiesArray[base] + " than " + complexitiesArray[base+1];
        }
    }
}
