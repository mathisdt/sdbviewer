package org.zephyrsoft.sdbviewer;

import android.app.Application;

import org.zephyrsoft.sdbviewer.fetch.SDBFetcher;

public class SDBViewerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SDBFetcher.createAndRegisterInstance();
    }
}