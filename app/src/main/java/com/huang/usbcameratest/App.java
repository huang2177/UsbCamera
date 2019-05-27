package com.huang.usbcameratest;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Des:
 * Created by huang on 2019/5/27 0027 14:12
 */
public class App extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
