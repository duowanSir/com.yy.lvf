package com.yy.lvf.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.os.FileObserver;

public class DynamicRecursiveFileObserver {
	private static class Instance {
		private DynamicRecursiveFileObserver mInstance = new DynamicRecursiveFileObserver();
	}

	public class FileObserverExt extends FileObserver {
		private String mDirectory;

		public FileObserverExt(String path) {
			super(path);
			mDirectory = path;
		}

		@Override
		public void onEvent(int event, String path) {
			if ((event & FileObserver.CREATE) != 0) {

			} else if ((event & FileObserver.CLOSE_WRITE) != 0) {

			}
		}

	}

	public class ScanDirImpl implements Callable<List<String>> {
		private final File mDirectory;

		public ScanDirImpl(File dir) {
			mDirectory = dir;
		}

		@Override
		public List<String> call() throws Exception {
			File[] children = mDirectory.listFiles();
			List<String> result = null;
			if (children != null) {
				result = new ArrayList<String>();
				for (File i : children) {
					if (i.isDirectory()) {
						result.add(i.getAbsolutePath());
					}
				}
			}
			return result;
		}

	}

	public interface Callback {
		void onCreate();

		void onCloseWrite();
	}

	private String mRoot;
	private volatile int mState;
	private List<String> mObservableFiles = new ArrayList<String>();
	private Map<String, FileObserverExt> mObservers = new HashMap<String, FileObserverExt>();

	private final Object mSync = new Object();
	private final ExecutorService mExecutorService = Executors.newFixedThreadPool(3);

	private DynamicRecursiveFileObserver() {
	}

	public void setRoot(String path) {
		synchronized (mSync) {
			if (path.equals(mRoot)) {
				mState = -1;
				return;
			}
			mRoot = path;
			generateObservers();
			mState = 0;
		}
	}

	private void generateObservers() {
		File root = new File(mRoot);
		if (!root.exists() || !root.isDirectory()) {
			return;
		}
		List<Future<List<String>>> tasks = new ArrayList<Future<List<String>>>();
		List<String> pendingGenerateDirs = new ArrayList<String>();

		pendingGenerateDirs.add(mRoot);
		boolean isDone = false;
		while (!isDone) {
			while (pendingGenerateDirs.size() > 0) {
				File dir = new File(pendingGenerateDirs.get(0));
				mObservableFiles.add(pendingGenerateDirs.get(0));
				pendingGenerateDirs.remove(0);
				Future<List<String>> f = mExecutorService.submit(new ScanDirImpl(dir));
				tasks.add(f);
			}
			for (Future<List<String>> i : tasks) {
				List<String> result = null;
				try {
					result = i.get(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
				if (result != null) {
					pendingGenerateDirs.addAll(result);
				}
			}
			if (pendingGenerateDirs.size() <= 0) {
				isDone = true;
			} else {
				isDone = false;
			}
		}
		for (String i : mObservableFiles) {
			mObservers.put(i, new FileObserverExt(i));
		}

	}

	public void start() {
		synchronized (mSync) {
			if (mState == 0) {
				for (String k : mObservableFiles) {
					FileObserverExt v = mObservers.get(k);
					v.startWatching();
				}
				mState = 1;
			}
		}
	}

	public void stop() {
		synchronized (mSync) {
			if (mState == 1) {
				for (String k : mObservableFiles) {
					FileObserverExt v = mObservers.get(k);
					v.stopWatching();
				}
			}
		}
	}

}
