package com.android.lvf.demo.component;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.lvf.ICompute;

public class ServiceRemoteCompute extends Service {
	public static final String	TAG	= "LearnAIPC";
	private ExtCompute			mExtCompute;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		mExtCompute = new ExtCompute();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind(" + intent + ")");
		return mExtCompute;
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart(" + intent + ", " + startId + ")");
	}

	public static class ExtCompute extends ICompute.Stub {

		@Override
		public int add(int a, int b) throws RemoteException {
			Log.d(TAG, "add(" + a + ", " + b + ", " + Thread.currentThread() + ")");
			return a + b;
		}

	}

}
