package com.android.lvf.demo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lvf.ICompute;
import com.android.lvf.R;
import com.android.lvf.demo.animation.ActivityPropertyAnimation;
import com.android.lvf.demo.component.ActivitySingleInstance;
import com.android.lvf.demo.component.ActivitySingleTask;
import com.android.lvf.demo.component.ActivitySingleTop;
import com.android.lvf.demo.component.ActivityStandard;
import com.android.lvf.demo.component.ServiceRemoteCompute;
import com.android.lvf.demo.db.DaoManager;
import com.android.lvf.demo.db.table.VideoInfo;
import com.android.lvf.demo.event.ActivityVideoList;
import com.android.lvf.demo.event.HorizontalSlideActivity;
import com.android.lvf.demo.surface.ActivitySurfaceCanvasUse;


public class Main extends Activity implements OnClickListener {
    TextView          mInfo;
    ServiceConnection mServiceConnection;
    IBinder           mBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInfo = (TextView) findViewById(R.id.info_tv);
        mInfo.setText(getClass().getName());
        mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(ServiceRemoteCompute.TAG, "onServiceDisconnected(" + name + ")");
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(ServiceRemoteCompute.TAG, "onServiceConnected(" + name + ", " + service + ", " + Thread.currentThread() + ")");
                ICompute iCompute = ICompute.Stub.asInterface(service);
                Log.d(ServiceRemoteCompute.TAG, "onServiceConnected(" + iCompute + ")");
                try {
                    iCompute.add(2, 3);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.standard) {
            Intent intent = new Intent(this, ActivityStandard.class);
            startActivity(intent);
        } else if (v.getId() == R.id.single_top) {
            Intent intent = new Intent(this, ActivitySingleTop.class);
            startActivity(intent);
        } else if (v.getId() == R.id.single_task) {
            Intent intent = new Intent(this, ActivitySingleTask.class);
            startActivity(intent);
        } else if (v.getId() == R.id.single_instance) {
            Intent intent = new Intent(this, ActivitySingleInstance.class);
            startActivity(intent);
        } else if (v.getId() == R.id.start_service) {
            Intent i = new Intent(this, ServiceRemoteCompute.class);
            startService(i);
        } else if (v.getId() == R.id.bind_service) {
            Intent i = new Intent(this, ServiceRemoteCompute.class);
            bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else if (v.getId() == R.id.stop_service) {
            Intent i = new Intent(this, ServiceRemoteCompute.class);
            stopService(i);
        } else if (v.getId() == R.id.unbind_service) {
            unbindService(mServiceConnection);
        } else if (v.getId() == R.id.surface_and_texture) {
            Intent intent = new Intent(this, ActivitySurfaceCanvasUse.class);
            startActivity(intent);
        } else if (v.getId() == R.id.video_list) {
            Intent intent = new Intent(this, ActivityVideoList.class);
            startActivity(intent);
        } else if (v.getId() == R.id.horizontal_slide) {
            Intent intent = new Intent(this, HorizontalSlideActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.animation) {
            Intent intent = new Intent(this, ActivityPropertyAnimation.class);
            startActivity(intent);
        } else if (v.getId() == R.id.insert) {
            long timeMs = System.currentTimeMillis();
            int timeS = (int) (timeMs / 1000);
            DaoManager.getInstance().getVideoInfoDao().insert(new VideoInfo(timeMs, timeS, timeS, timeS, false));
        } else if (v.getId() == R.id.update) {
        } else if (v.getId() == R.id.retrieve) {
        } else if (v.getId() == R.id.delete) {
        } else {
            throw new RuntimeException("unprocessed click event");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Toast.makeText(this, getClass().getName(), Toast.LENGTH_SHORT).show();
        super.onNewIntent(intent);
    }

}
