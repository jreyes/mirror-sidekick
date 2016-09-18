package com.vaporwarecorp.mirror.sidekick;

import android.app.Application;
import timber.log.Timber;

public class SidekickApp extends Application {
// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
