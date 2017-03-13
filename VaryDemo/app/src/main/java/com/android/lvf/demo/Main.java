package com.android.lvf.demo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lvf.ICompute;
import com.android.lvf.LLog;
import com.android.lvf.R;
import com.android.lvf.demo.algorithm.substate.LeastCoin;
import com.android.lvf.demo.animation.ActivityPropertyAnimation;
import com.android.lvf.demo.component.ActivitySingleInstance;
import com.android.lvf.demo.component.ActivitySingleTask;
import com.android.lvf.demo.component.ActivitySingleTop;
import com.android.lvf.demo.component.ActivityStandard;
import com.android.lvf.demo.component.BroadcastReceiverTest;
import com.android.lvf.demo.component.ServiceRemoteCompute;
import com.android.lvf.demo.db.dao.VideoInfoDao;
import com.android.lvf.demo.db.table.VideoInfo;
import com.android.lvf.demo.event.ActivityVideoList;
import com.android.lvf.demo.event.HorizontalSlideActivity;
import com.android.lvf.demo.surface.ActivitySurfaceCanvasUse;
import com.android.lvf.demo.surface.ActivityTestCamera;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main extends Activity implements OnClickListener {
    TextView          mInfo;
    ServiceConnection mServiceConnection;

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

        testLeastCoin();
    }

    private void testLeastCoin() {
        LeastCoin leastCoin = new LeastCoin();
        leastCoin.reachSum(11, new int[]{2, 3, 5});
        Iterator<Map.Entry<Integer, Integer>> iterator = leastCoin.mSubStates.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
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
            testConcurrentDatabase();
        } else if (v.getId() == R.id.update) {
        } else if (v.getId() == R.id.retrieve) {
        } else if (v.getId() == R.id.delete) {
        } else if (v.getId() == R.id.alarm_receiver) {
            testAlarm(BroadcastReceiverTest.class);
        } else if (v.getId() == R.id.show_camera_info) {
            testCameraInfo();
        } else if (v.getId() == R.id.test_camera) {
            Intent intent = new Intent(this, ActivityTestCamera.class);
            startActivity(intent);
        } else {
            throw new RuntimeException("unprocessed click event");
        }
    }

    private void testAlarm(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        String superClassName = clazz.getSuperclass().getSimpleName();
        if (TextUtils.isEmpty(superClassName)) {
            return;
        }
        superClassName = superClassName.toLowerCase();
        PendingIntent pi;
        if (superClassName.equals("service")) {
            pi = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        } else if (superClassName.equals("broadcastreceiver")) {
            pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            return;
        }
        AlarmManager alarmManager = getSystemService(AlarmManager.class);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 10000, pi);
    }

    private void testCameraInfo() {
        int cameraNum = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraNum; i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            LLog.d("CameraInfo", "[" + cameraInfo.facing + ", " + cameraInfo.orientation + ", " + cameraInfo.canDisableShutterSound + "]");
        }
    }

    private void testConcurrentDatabase() {
        final List<Integer> timeCost = new ArrayList<>();
        final ExecutorService threadPool = Executors.newFixedThreadPool(10);
        final VideoInfoDao vid = new VideoInfoDao();
        for (int i = 1; i < 1001; i++) {
            final int j = i;
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    LLog.d("Main", "多任务,多事务");
                    long timestamp = SystemClock.elapsedRealtime();
                    if (j % 2 == 0) {
                        VideoInfo vi = new VideoInfo(SystemClock.elapsedRealtime(), j, j, j, true, j);
                        vid.insert(vi);
                    } else {
                        List<VideoInfo> vis = new ArrayList<>();
                        for (int m = 1; m < 11; m++) {
                            vis.add(new VideoInfo((long) (j * m), m, m, m, false, m));
                        }
                        vid.insert(vis, "OR REPLACE");
                    }
                    long timestamp1 = SystemClock.elapsedRealtime();
                    synchronized (timeCost) {
                        timeCost.add((int) (timestamp1 - timestamp));
                        if (timeCost.size() == 1000) {
                            int sum = 0;
                            for (int i : timeCost
                                    ) {
                                sum += i;
                            }
                            LLog.d("Main", sum + "," + (float) sum / 1000);
                        }
                    }
                }
            };
            threadPool.submit(task);
        }
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                LLog.d("Main", "单任务,单事务");
                long timestamp = SystemClock.elapsedRealtime();
                List<VideoInfo> vis = new ArrayList<>();
                for (int m = 1; m < 1000; m++) {
                    vis.add(new VideoInfo(SystemClock.elapsedRealtime(), m, m, m, false, m));
                }
                vid.insert(vis, "OR REPLACE");
                long timestamp1 = SystemClock.elapsedRealtime();
                LLog.d("Main", "Transaction:" + (timestamp1 - timestamp));
            }
        });

        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                LLog.d("Main", "单任务,多事务");
                long timestamp = SystemClock.elapsedRealtime();
                for (int m = 1; m < 1000; m++) {
                    VideoInfo vi = new VideoInfo(SystemClock.elapsedRealtime(), m, m, m, false, m);
                    vid.insert(vi);
                }
                long timestamp1 = SystemClock.elapsedRealtime();
                LLog.d("Main", "NonTransaction:" + (timestamp1 - timestamp));
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Toast.makeText(this, getClass().getName(), Toast.LENGTH_SHORT).show();
        super.onNewIntent(intent);
    }

}
