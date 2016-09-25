package com.yy.lvf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

public class FileObserveService extends Service {
    public static String TAG = FileObserveService.class.getSimpleName();
    public static boolean VERBOSE = false;
    private FileObserveBinder mBinder;
    private ExecutorService mThreadPool;

    @Override
    public void onCreate() {
	// 仅当第一次被创建时回调
	super.onCreate();
	if (VERBOSE) {
	    Log.d(TAG, "onCreate");
	}
	mBinder = new FileObserveBinder();
	mThreadPool = Executors.newFixedThreadPool(2);
    }

    @Override
    public void onDestroy() {
	// 仅当服务停止且没有被关联时回调
	super.onDestroy();
	if (VERBOSE) {
	    Log.d(TAG, "onDestroy");
	}
	mBinder = null;
	mThreadPool.shutdown();
	mThreadPool = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	// context.startService时回调
	if (VERBOSE) {
	    Log.d(TAG, "onStartCommand");
	}
	return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
	if (VERBOSE) {
	    Log.d(TAG, "onBind");
	}
	return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
	if (VERBOSE) {
	    Log.d(TAG, "onUnbind");
	}
	return super.onUnbind(intent);
    }

    public static class FileObserveBinder extends Binder {
	public void startObserveFile() {
	    if (VERBOSE) {
		Log.d(TAG, "startObserveFile");
	    }
	}

	public void stopObserveFile() {
	    if (VERBOSE) {
		Log.d(TAG, "stopObserveFile");
	    }
	}

	public void setTargetFile(File file) {

	}

	public void transcodeVideo() {
	    if (VERBOSE) {
		Log.d(TAG, "transcodeVideo");
	    }
	}
    }

    public class WxTimeLineVideoObserver extends FileObserver {
	// linux inotify机制，无法递归监听。
	private File mTargetVideo;
	private File mTargetThumb;

	public WxTimeLineVideoObserver(String path) {
	    super(path);
	}

	public boolean setupTarget(File video, File thumb) {
	    if (video == null || !video.exists() || !video.isFile()) {
		return false;
	    }
	    if (thumb == null || !thumb.exists() || !thumb.isFile()) {
		return false;
	    }
	    mTargetVideo = video;
	    mTargetThumb = thumb;
	    File videoRecordDir = getWxTimeLineVideoRecordDir();
	    if (videoRecordDir == null) {
		mTargetVideo = null;
		mTargetThumb = null;
		return false;
	    }
	    File mTempVideo = new File(videoRecordDir, mTargetVideo.getName());
	    File mTempThumb = new File(videoRecordDir, mTargetThumb.getName());
	    if (mTempVideo.exists()) {
		mTempVideo.delete();
	    }
	    if (mTempThumb.exists()) {
		mTempThumb.delete();
	    }
	    BufferedOutputStream os = null;
	    BufferedInputStream is = null;
	    byte[] buffer = new byte[1024];
	    int size = 0;
	    try {
		is = new BufferedInputStream(new FileInputStream(mTargetVideo));
		os = new BufferedOutputStream(new FileOutputStream(mTempVideo));
		while ((size = is.read(buffer)) != -1) {
		    os.write(buffer, 0, size);
		}
		os.flush();
	    } catch (Exception e) {
		e.printStackTrace();
		return false;
	    } finally {
		try {
		    if (is != null) {
			is.close();
		    }
		    if (os != null) {
			os.close();
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		}
		is = null;
		os = null;
	    }
	    try {
		is = new BufferedInputStream(new FileInputStream(mTargetThumb));
		os = new BufferedOutputStream(new FileOutputStream(mTempThumb));
		while ((size = is.read(buffer)) != -1) {
		    os.write(buffer, 0, size);
		}
		os.flush();
	    } catch (Exception e) {
		e.printStackTrace();
		return false;
	    } finally {
		try {
		    if (is != null) {
			is.close();
		    }
		    if (os != null) {
			os.close();
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		}
		is = null;
		os = null;
	    }
	    return true;
	}

	@Override
	public void onEvent(int event, String path) {

	}

	public File getWxTimeLineVideoRecordDir() {
	    return null;
	}

    }

}
