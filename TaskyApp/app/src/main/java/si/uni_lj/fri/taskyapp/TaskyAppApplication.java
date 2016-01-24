package si.uni_lj.fri.taskyapp;

import android.app.Application;

import com.orm.SugarContext;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class TaskyAppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }
}
