package com.nixholas.materialtunes;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by nixholas on 8/3/17.
 */

public class CoreApplication extends Application {
    private static CoreApplication mInstance;

    public static synchronized CoreApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...
    }
}
