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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.os.FileObserver;
import android.util.Log;

public class DynamicRecursiveFileObserver {
	private static class Instance {
		private DynamicRecursiveFileObserver	mInstance	= new DynamicRecursiveFileObserver();
	}

	public class FileObserverExt extends FileObserver {
		private String	mDirectory;

		public FileObserverExt(String path) {
			super(path);
			mDirectory = path;
		}

		@Override
		public void onEvent(int event, String path) {
			if ((event & FileObserver.CREATE) != 0) {
				File newFile = new File(mDirectory, path);
				if (newFile.isDirectory()) {
					try {
						if (mLock.writeLock().tryLock(100, TimeUnit.MILLISECONDS)) {
							if (mState != 1) {
								throw new IllegalStateException("");
							} else {
								String newPath = newFile.getAbsolutePath();
								mObservableFiles.add(newPath);
								FileObserverExt newObserver = new FileObserverExt(newPath);
								mObservers.put(newPath, newObserver);
								newObserver.startWatching();
							}
						} else {
							logLock(mLock.writeLock());
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						mLock.writeLock().unlock();
					}
				}
				try {

				} catch (Exception e) {
				} finally {

				}
			} else if ((event & FileObserver.CLOSE_WRITE) != 0) {

			}
		}

	}

	public class ScanDirImpl implements Callable<List<String>> {
		private final File	mDirectory;

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
		void onCreate(final String path);

		void onCloseWrite(final String path);
	}

	public static final boolean				VERBOSE				= true;
	public static final String				TAG					= DynamicRecursiveFileObserver.class.getSimpleName();
	private String							mRoot;
	private final ReadWriteLock				mLock				= new ReentrantReadWriteLock();
	private int								mState;
	private List<String>					mObservableFiles	= new ArrayList<String>();
	private Map<String, FileObserverExt>	mObservers			= new HashMap<String, FileObserverExt>();

	private final ExecutorService			mExecutorService	= Executors.newFixedThreadPool(3);
	private final ReentrantLock				mCallbackLock		= new ReentrantLock();
	private Callback						mCallback;

	private DynamicRecursiveFileObserver() {
	}

	public void setRoot(String path) {
		try {
			if (mLock.writeLock().tryLock(100, TimeUnit.MILLISECONDS)) {
				if (path.equals(mRoot)) {
					return;
				}
				if (mState != 0) {
					throw new IllegalStateException("");
				}
				mRoot = path;
				generateObservers();
				mState = 0;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			mLock.writeLock().unlock();
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
		boolean done = false;
		try {
			if (mLock.readLock().tryLock(100, TimeUnit.MILLISECONDS)) {
				if (mState == 0) {
					for (String k : mObservableFiles) {
						FileObserverExt v = mObservers.get(k);
						v.startWatching();
					}
					done = true;
				}
			}
		} catch (InterruptedException e) {
		} finally {
			mLock.readLock().unlock();
		}
		if (done) {
			try {
				if (mLock.writeLock().tryLock(100, TimeUnit.MILLISECONDS)) {
					mState = 1;
				}
			} catch (InterruptedException e) {
			} finally {
				mLock.writeLock().unlock();
			}
		}
	}

	public void stop() {
		boolean done = false;
		try {
			if (mLock.readLock().tryLock(100, TimeUnit.MILLISECONDS)) {
				if (mState == 1) {
					for (String k : mObservableFiles) {
						FileObserverExt v = mObservers.get(k);
						v.stopWatching();
					}
					done = true;
				}
			}
		} catch (InterruptedException e) {
		} finally {
			mLock.readLock().unlock();
		}
		if (done) {
			try {
				if (mLock.writeLock().tryLock(100, TimeUnit.MILLISECONDS)) {
					mState = 0;
				}
			} catch (InterruptedException e) {
			} finally {
				mLock.writeLock().unlock();
			}
		}
	}

	public void setCallback(Callback callback) {
		try {
			if (mCallbackLock.tryLock(10, TimeUnit.MILLISECONDS)) {
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {

		}
	}

	public static void logLock(Lock lock) {
		if (VERBOSE) {
			Log.d(TAG, Thread.currentThread() + " lock " + lock.toString() + " failed");
		}
	}

}
