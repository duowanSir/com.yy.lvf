package com.yy.lvf.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.text.TextUtils;
import android.util.Log;

import com.yy.lvf.util.DynamicRecursiveFileObserver.Callback;

public class WxTimeLineVideoThief implements Callback {
	private File			mVideo;
	private File			mThumb;
	private int				mCopyTimes			= 0;
	private final Lock		mLock				= new ReentrantLock();
	private ExecutorService	mExecutorService	= Executors.newSingleThreadExecutor();

	public WxTimeLineVideoThief(File video, File thumb) {
		mVideo = video;
		mThumb = thumb;
	}

	@Override
	public void onCreate(final String father, final String path) {
		Log.d("WxTimeLineVideoThief", "onCreat(" + father + ", " + path + ")");
		if (!TextUtils.isEmpty(father) && !TextUtils.isEmpty(path) && path.contains("temp")) {
			final boolean isVideo = path.endsWith(".mp4");
			final boolean isThumb = path.endsWith(".thumb");
			if (isVideo || isThumb) {
				final File dir = new File(father);
				mExecutorService.submit(new Runnable() {

					@Override
					public void run() {
						try {
							if (mLock.tryLock(100, TimeUnit.MILLISECONDS)) {
								if (isThumb && (mCopyTimes & 1) == 0) {
									mThumb = copyFile(mThumb, dir);
									mCopyTimes = mCopyTimes | 1;
								} else if (isVideo && (mCopyTimes & 2) == 0) {
									mVideo = copyFile(mVideo, dir);
									mCopyTimes = mCopyTimes | 2;
								}
								if (mCopyTimes == 3) {
									Log.d("WxTimeLineVideoThief", "À… ÷ÃÊªª");
								}
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} finally {
							mLock.unlock();
						}
					}
				});
			}
		}
	}

	@Override
	public void onCloseWrite(final String father, final String path) {
		Log.d("WxTimeLineVideoThief", "onCloseWrite(" + father + ", " + path + ")");
		if (!TextUtils.isEmpty(father) && !TextUtils.isEmpty(path) && path.contains("temp")) {
			final boolean isVideo = path.endsWith(".mp4");
			final boolean isThumb = path.endsWith(".thumb");
			if (isVideo || isThumb) {
				final File dir = new File(father);
				mExecutorService.submit(new Runnable() {

					@Override
					public void run() {
						try {
							if (mLock.tryLock(100, TimeUnit.MILLISECONDS)) {
								if (isThumb && (mCopyTimes & 1) == 0) {
									mThumb.renameTo(new File(dir, path));
								} else if (isVideo && (mCopyTimes & 2) == 0) {
									mVideo.renameTo(new File(dir, path));
								}
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} finally {
							mLock.unlock();
						}
					}
				});
			}
		}
	}

	public static File copyFile(File srcFile, File dstDir) {
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		byte[] buffer = new byte[1024];
		int size = 0;
		File dstFile = new File(dstDir, srcFile.getName());
		if (dstFile.exists()) {
			dstFile.delete();
		}
		try {
			bos = new BufferedOutputStream(new FileOutputStream(dstFile));
			bis = new BufferedInputStream(new FileInputStream(srcFile));
			while ((size = bis.read(buffer)) != -1) {
				bos.write(buffer, 0, size);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bos.flush();
				bos.close();
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dstFile;
	}

}
