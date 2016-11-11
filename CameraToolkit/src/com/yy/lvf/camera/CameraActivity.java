package com.yy.lvf.camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public class CameraActivity extends Activity implements OnClickListener {
	public static final String	TAG	= CameraActivity.class.getSimpleName();
	private GLSurfaceView		mPreviewGlsv;
	private Button				mRecordVideoBtn;
	private Button				mTakePictureBtn;
	private Button				mSwitchCameraBtn;

	private int					mCameraId;
	private Camera				mCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity);

		mPreviewGlsv = (GLSurfaceView) findViewById(R.id.preview_glsv);
		mRecordVideoBtn = (Button) findViewById(R.id.record_video_btn);
		mTakePictureBtn = (Button) findViewById(R.id.take_picture_btn);
		mSwitchCameraBtn = (Button) findViewById(R.id.switch_camera_btn);
		mRecordVideoBtn.setOnClickListener(this);
		mTakePictureBtn.setOnClickListener(this);
		mSwitchCameraBtn.setOnClickListener(this);
		mPreviewGlsv.setRenderer(new Renderer() {

			@Override
			public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			}

			@Override
			public void onSurfaceChanged(GL10 gl, int width, int height) {
			}

			@Override
			public void onDrawFrame(GL10 gl) {
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mCamera = getCameraInstance(mCameraId);
		setPreviewSize();
		
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
				LayoutParams previewLp = mPreviewGlsv.getLayoutParams();
				previewLp.width = width;
				previewLp.height = height;
				mPreviewGlsv.setLayoutParams(previewLp);
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
