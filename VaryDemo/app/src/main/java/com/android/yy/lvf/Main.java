package com.android.yy.lvf;

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
                Log.d(RemoteComputService.TAG, "onServiceDisconnected(" + name + ")");
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(RemoteComputService.TAG, "onServiceConnected(" + name + ", " + service + ", " + Thread.currentThread() + ")");
                ICompute iCompute = ICompute.Stub.asInterface(service);
                Log.d(RemoteComputService.TAG, "onServiceConnected(" + iCompute + ")");
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
            Intent intent = new Intent(this, Standard.class);
            startActivity(intent);
        } else if (v.getId() == R.id.single_top) {
            Intent intent = new Intent(this, SingleTop.class);
            startActivity(intent);
        } else if (v.getId() == R.id.single_task) {
            Intent intent = new Intent(this, SingleTask.class);
            startActivity(intent);
        } else if (v.getId() == R.id.single_instance) {
            Intent intent = new Intent(this, SingleInstance.class);
            startActivity(intent);
        } else if (v.getId() == R.id.start_service) {
            Intent i = new Intent(this, RemoteComputService.class);
            startService(i);
        } else if (v.getId() == R.id.bind_service) {
            Intent i = new Intent(this, RemoteComputService.class);
            bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else if (v.getId() == R.id.stop_service) {
            Intent i = new Intent(this, RemoteComputService.class);
            stopService(i);
        } else if (v.getId() == R.id.unbind_service) {
            unbindService(mServiceConnection);
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
