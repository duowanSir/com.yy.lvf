package com.yy.lvf.camera;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CameraActivity extends Activity implements OnClickListener {
	public static class CameraAndSurface implements Callback {
		private Camera			mCamera;
		private SurfaceView		mSurfaceView;
		private SurfaceHolder	mSurfaceHoldr;

		public CameraAndSurface(Camera camera, SurfaceView surfaceView) {
			mCamera = camera;
			mSurfaceView = surfaceView;
			mSurfaceHoldr = mSurfaceView.getHolder();
			mSurfaceHoldr.addCallback(this);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Log.d(TAG, "surfaceChanged(" + holder + ", " + format + ", " + width + ", " + height + ")");
			if (holder.getSurface() == null) {
				return;
			}

			mCamera.stopPreview();

			float surfaceRatio = height / width;
			Parameters params = mCamera.getParameters();
			List<Size> previewSizes = params.getSupportedPreviewSizes();
			if (previewSizes != null && !previewSizes.isEmpty()) {
				float ratio = 100;
				Size s = null;
				for (int i = 0; i < previewSizes.size(); i++) {
					s = previewSizes.get(i);
					//					float r = (float) s.height / s.width;
					float r = (float) s.width / s.height;
					if (r >= surfaceRatio && r <= ratio) {
						ratio = r;
					}
				}
				Log.d(TAG, "ratio[" + surfaceRatio + ", " + ratio + "]");
				if (ratio != 100) {
					params.setPreviewSize(s.width, s.height);
				}
			}
			mCamera.setDisplayOrientation(90);

			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.setPreviewTexture(null);
				mCamera.startPreview();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			mCamera.startPreview();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			mCamera.release();
			mCamera = null;
		}

	}

	public static final String	TAG				= CameraActivity.class.getSimpleName();
	private SurfaceView			mCameraSurfaceView;
	private Button				mStartBtn;
	private Button				mSwitchCameraBtn;
	private int					mCameraId;
	private Camera				mCamera;

	private float				mSurfaceRatio	= 1.33f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity);

		mCameraSurfaceView = (SurfaceView) findViewById(R.id.camera_surface_view);
		mStartBtn = (Button) findViewById(R.id.start_btn);
		mSwitchCameraBtn = (Button) findViewById(R.id.switch_btn);
		mStartBtn.setOnClickListener(this);
		mSwitchCameraBtn.setOnClickListener(this);

		mCamera = getCameraInstance(mCameraId);
		CameraAndSurface cs = new CameraAndSurface(mCamera, mCameraSurfaceView);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			//			Rect decorRect = new Rect();
			//			getWindow().getDecorView().getWindowVisibleDisplayFrame(decorRect);

			Rect surfaceRect = new Rect();
			mCameraSurfaceView.getWindowVisibleDisplayFrame(surfaceRect);
			Log.d(TAG, "surfaceRect[" + surfaceRect.left + ", " + surfaceRect.top + ", " + surfaceRect.right + ", " + surfaceRect.bottom + "]");
			mSurfaceRatio = (float) surfaceRect.height() / surfaceRect.width();

			//			Rect surfaceDrawingRect = new Rect();
			//			mCameraSurfaceView.getDrawingRect(surfaceDrawingRect);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mStartBtn) {

		} else if (v == mSwitchCameraBtn) {
			releaseCamera();
			mCameraId = (++mCameraId) % Camera.getNumberOfCameras();
			mCamera = getCameraInstance(mCameraId);
			CameraAndSurface cs = new CameraAndSurface(mCamera, mCameraSurfaceView);
		}
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
			throw new IllegalArgumentException("找不到指定相机");
		}
		Camera c = null;
		try {

			c = Camera.open(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	public void checkCameraCapabilities(int cameraId, Camera camera) {
		Parameters param = camera.getParameters();
		if (Build.VERSION.SDK_INT >= 9) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(cameraId, info);
		}
	}
}
