package com.android.lvf.demo;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;

import com.android.lvf.LLog;
import com.android.lvf.demo.component.ServiceRemoteCompute;
import com.android.lvf.demo.db.DatabaseManager;

import java.util.List;

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
        if ("com.android.lvf".equals(getProcessName(this, Process.myPid()))) {
            LLog.d("LApplication","初始化数据库");
            DatabaseManager.getInstance().init(this);
        }
    }

    public static String getProcessName(Context context, int pid) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfoList = am.getRunningAppProcesses();
        if (processInfoList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : processInfoList) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }
}
