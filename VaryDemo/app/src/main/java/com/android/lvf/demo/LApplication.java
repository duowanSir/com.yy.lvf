package com.android.lvf.demo;

import android.app.Application;

import com.android.lvf.demo.db.DaoManager;

/**
 * Created by slowergun on 2016/12/15.
 */
public class LApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initDb();
    }

    private void initDb() {
        DaoManager.getInstance().init(this);
    }
}
