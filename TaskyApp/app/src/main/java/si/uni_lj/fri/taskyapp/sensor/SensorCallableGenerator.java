package si.uni_lj.fri.taskyapp.sensor;

import android.util.Log;

import com.ubhave.sensormanager.ESSensorManager;
import com.ubhave.sensormanager.data.SensorData;

import java.util.concurrent.Callable;

/**
 * Created by urgas9 on 22. 01. 2016.
 */
public class SensorCallableGenerator {

    public static Callable<SensorData> getSensorDataCallable(final ESSensorManager sm, final int sensorId){
        return new Callable<SensorData>() {
            @Override
            public SensorData call() throws Exception {
                Log.d("S", "Returning a new callable");
                return sm.getDataFromSensor(sensorId);
            }
        };
    }
}
