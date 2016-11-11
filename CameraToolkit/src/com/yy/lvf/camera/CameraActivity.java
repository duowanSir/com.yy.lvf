package com.yy.lvf.camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import com.yy.lvf.LLog;
import com.yy.lvf.gles.EglCore;
import com.yy.lvf.gles.GlUtil;
import com.yy.lvf.gles.Texture2dProgram;
import com.yy.lvf.gles.WindowSurface;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.TextureView.SurfaceTextureListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public class CameraActivity extends Activity implements OnClickListener, Callback {
	public static class RenderThread extends Thread implements SurfaceTextureListener {
		// Used to wait for the thread to start.
		private Object				mStartLock	= new Object();
		private boolean				mReady		= false;
		private RenderHandler		mRenderHandler;

		private EglCore				mEglCore;
		private WindowSurface		mWindowSurface;

		private SurfaceTexture		mCameraTexture;

		private Texture2dProgram	mTexProgram;

		@Override
		public void run() {
			super.run();
			Looper.prepare();
			mRenderHandler = new RenderHandler(this);
			synchronized (mStartLock) {
				mReady = true;
				mStartLock.notify();
			}
			mEglCore = new EglCore(null, 0);

			Looper.loop();

			releaseGl();
			mEglCore.release();
		}

		public void waitUtilReady() throws InterruptedException {
			synchronized (mStartLock) {
				while (!mReady) {
					mStartLock.wait();
				}
			}
		}

		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			return false;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		}

		public RenderHandler getRenderHandler() {
			return mRenderHandler;
		}

		private void releaseGl() {
			GlUtil.checkGlError("releaseGl start");

			if (mWindowSurface != null) {
				mWindowSurface.release();
				mWindowSurface = null;
			}
			if (mTexProgram != null) {
				mTexProgram.release();
				mTexProgram = null;
			}
			GlUtil.checkGlError("releaseGl done");

			mEglCore.makeNothingCurrent();
		}
	}

	public static class RenderHandler extends Handler {
		public static final int				MSG_SURFACE_CREATED	= 0;
		private WeakReference<RenderThread>	mRenderThread;

		public RenderHandler(RenderThread thread) {
			mRenderThread = new WeakReference<CameraActivity.RenderThread>(thread);
		}

		@Override
		public void handleMessage(Message msg) {
			RenderThread renderThread = mRenderThread.get();
			if (renderThread == null) {
				removeCallbacksAndMessages(null);
				return;
			}
			switch (msg.what) {
			case MSG_SURFACE_CREATED:
				SurfaceHolder surfaceHolder = (SurfaceHolder) msg.obj;
				Surface surface = surfaceHolder.getSurface();
				renderThread.mWindowSurface = new WindowSurface(renderThread.mEglCore, surface, false);
				renderThread.mWindowSurface.makeCurrent();

				// Create and configure the SurfaceTexture, which will receive frames from the
				// camera.  We set the textured rect's program to render from it.
				renderThread.mTexProgram = new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT);
				int textureId = renderThread.mTexProgram.createTextureObject();
				renderThread.mCameraTexture = new SurfaceTexture(textureId);
				//	            mRect.setTexture(textureId);
//				renderThread.mCameraTexture.setOnFrameAvailableListener(this);
				break;
			default:
				break;
			}
		}
	}

	public static final String	TAG	= CameraActivity.class.getSimpleName();
	private SurfaceView			mPreviewSv;
	private Button				mRecordVideoBtn;
	private Button				mTakePictureBtn;
	private Button				mSwitchCameraBtn;

	private int					mCameraId;
	private Camera				mCamera;									//camera应该的放在onResume和onPause里面去管理

	private RenderThread		mRenderThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity);

		mPreviewSv = (SurfaceView) findViewById(R.id.preview_sv);
		mRecordVideoBtn = (Button) findViewById(R.id.record_video_btn);
		mTakePictureBtn = (Button) findViewById(R.id.take_picture_btn);
		mSwitchCameraBtn = (Button) findViewById(R.id.switch_camera_btn);
		mRecordVideoBtn.setOnClickListener(this);
		mTakePictureBtn.setOnClickListener(this);
		mSwitchCameraBtn.setOnClickListener(this);
		SurfaceHolder surfaceHolder = mPreviewSv.getHolder();
		LLog.d(TAG, surfaceHolder + "");
		surfaceHolder.addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		LLog.d(TAG, "surfaceCreated(" + holder + ", " + Thread.currentThread() + ")");
		try {
			mCamera.setPreviewDisplay(mPreviewSv.getHolder());
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		LLog.d(TAG, "surfaceChanged(" + holder + ", " + format + ", " + width + ", " + height + ")");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		LLog.d(TAG, "surfaceDestroyed(" + holder + ")");
	}

	@Override
	protected void onResume() {
		super.onResume();
		mCamera = getCameraInstance(mCameraId);
		setPreviewSize();

		mRenderThread = new RenderThread();
		mRenderThread.start();
		try {
			mRenderThread.waitUtilReady();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
	}

	@Override
	public void onClick(View v) {
		if (v == mRecordVideoBtn) {
			mCamera.takePicture(null, null, new PictureCallback() {

				@Override
				public void onPictureTaken(byte[] data, Camera camera) {
					File pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
					File pictureFile = new File(pictureDir, "cameraToolkit.jpg");
					if (pictureFile.exists()) {
						pictureFile.delete();
					}
					BufferedOutputStream bos = null;
					try {
						bos = new BufferedOutputStream(new FileOutputStream(pictureFile));
						bos.write(data);
						bos.flush();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (bos != null) {
							try {
								bos.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			});
		} else if (v == mSwitchCameraBtn) {
			releaseCamera();
			mCameraId = (++mCameraId) % Camera.getNumberOfCameras();
			mCamera = getCameraInstance(mCameraId);
		}
	}

	private void setPreviewSize() {
		Parameters params = mCamera.getParameters();
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(mCameraId, info);
		Log.d(TAG, "cameraInfo[" + info.facing + ", " + info.orientation + "]");
		int displayOrientation = getWindowManager().getDefaultDisplay().getOrientation();
		switch (displayOrientation) {
		case Surface.ROTATION_0:
			displayOrientation = 0;
			break;
		case Surface.ROTATION_90:
			displayOrientation = 90;
			break;
		case Surface.ROTATION_180:
			displayOrientation = 180;
			break;
		case Surface.ROTATION_270:
			displayOrientation = 270;
			break;
		default:
			throw new IllegalArgumentException("invalid display orientaion");
		}
		int cameraDisplayOrientation = 0;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			cameraDisplayOrientation = (info.orientation + displayOrientation) % 360;
			cameraDisplayOrientation = (360 - cameraDisplayOrientation) % 360;
		} else {
			cameraDisplayOrientation = (info.orientation - displayOrientation + 360) % 360;
		}
		List<Size> previewSizes = params.getSupportedPreviewSizes();
		for (Size size : previewSizes) {
			int width;
			int height;
			if (cameraDisplayOrientation == 0 || cameraDisplayOrientation == 180) {
				width = size.width;
				height = size.height;
			} else {
				width = size.height;
				height = size.width;
			}
			if (width == getResources().getDisplayMetrics().widthPixels) {
				LayoutParams previewLp = mPreviewSv.getLayoutParams();
				previewLp.width = width;
				previewLp.height = height;
				mPreviewSv.setLayoutParams(previewLp);
				params.setPreviewSize(size.width, size.height);
				mCamera.setParameters(params);
				break;
			}
		}
		mCamera.setDisplayOrientation(cameraDisplayOrientation);
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	public static Camera getCameraInstance(int i) {
		int cameraNums = Camera.getNumberOfCameras();
		if (i > cameraNums) {
			throw new IllegalArgumentException("invalid camera id");
		}
		Camera c = null;
		try {
			c = Camera.open(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

}
