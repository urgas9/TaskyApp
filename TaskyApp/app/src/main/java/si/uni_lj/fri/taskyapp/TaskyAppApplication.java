package si.uni_lj.fri.taskyapp;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.orm.SugarContext;
import com.squareup.leakcanary.LeakCanary;

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
        LeakCanary.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }
}
