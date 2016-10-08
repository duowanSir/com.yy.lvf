package com.yy.lvf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class FileObserveService extends Service {
	public static String TAG = FileObserveService.class.getSimpleName();
	public static boolean VERBOSE = false;
	private FileObserveBinder mBinder;
	private ExecutorService mThreadPool;
	private Handler mUiThreadHandler;
	private File mTargetVideo;
	private File mTargetThumb;

	@Override
	public void onCreate() {
		// 仅当第一次被创建时回调
		super.onCreate();
		if (VERBOSE) {
			Log.d(TAG, "onCreate");
		}
		mBinder = new FileObserveBinder();
		mThreadPool = Executors.newFixedThreadPool(2);
		mUiThreadHandler = new Handler(getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					Toast.makeText(getApplication(), "直发朋友圈准备就绪", Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}
		};
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
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (VERBOSE) {
			Log.d(TAG, "onUnbind");
		}
		return super.onUnbind(intent);
	}

	public File getWxTimeLineVideoRecordDir() {
		File parent = new File(Environment.getExternalStorageDirectory(), "tencent");
		File[] children = null;

		if (parent.exists() && parent.isDirectory()) {
			children = parent.listFiles();
			for (File child : children) {
				if ("micromsg".equalsIgnoreCase(child.getName()) && child.isDirectory()) {
					parent = child;
					break;
				}
			}

			children = parent.listFiles();
			for (File child : children) {
				if (child.isDirectory()) {
					int flag = 0;
					for (File file : child.listFiles()) {
						if (child.isDirectory()) {
							if ("draft".equalsIgnoreCase(file.getName())) {
								flag++;
							} else if ("video".equalsIgnoreCase(file.getName())) {
								flag++;
								parent = file;
							}
						}
					}
					if (flag != 2) {
						parent = null;
					} else {
						break;
					}
				}
			}
		}
		return parent;
	}

	public class FileObserveBinder extends Binder {
		public boolean setupTarget(final File video, final File thumb) {
			Future<Boolean> result = mThreadPool.submit(new Callable<Boolean>() {

				@Override
				public Boolean call() throws Exception {
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
					mUiThreadHandler.sendEmptyMessage(0);
					return true;
				}
			});
			try {
				return result.get(1000, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
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

		public WxTimeLineVideoObserver(String path) {
			super(path);
		}

		// linux inotify机制，无法递归监听。
		@Override
		public void onEvent(int event, String path) {

		}

	}

}
