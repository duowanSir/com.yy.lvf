package com.android.lvf.demo;

import android.app.Application;
import android.os.Process;

import com.android.lvf.LLog;
import com.android.lvf.demo.component.ServiceRemoteCompute;
import com.android.lvf.demo.db.DaoManager;

/**
 * Created by slowergun on 2016/12/15.
 */
public class LApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LLog.d(ServiceRemoteCompute.TAG, "onCreate(" + Process.myPid() + ", " + Process.myTid() + ", " + Process.myUid() + ", " + Thread.currentThread() + ")");
        initDb();
    }

    private void initDb() {
        DaoManager.getInstance().init(this);
    }
}
