package net.mmho.photomap2;

import android.app.Application;

import ollie.Ollie;

public class PhotoMapApplication extends Application {

    private final static String DB_NAME="photomap.db";
    private final static int DB_VERSION = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        Ollie.with(this)
            .setName(DB_NAME)
            .setVersion(DB_VERSION)
            .setLogLevel(Ollie.LogLevel.FULL)
            .init();
    }
}
