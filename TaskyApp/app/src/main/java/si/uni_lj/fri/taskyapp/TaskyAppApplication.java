package si.uni_lj.fri.taskyapp;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.orm.SugarContext;

import io.fabric.sdk.android.Fabric;

/**
 * Created by urgas9 on 24. 01. 2016.
 */
public class TaskyAppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(getApplicationContext());

        Fabric.with(this, new Crashlytics());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }
}
