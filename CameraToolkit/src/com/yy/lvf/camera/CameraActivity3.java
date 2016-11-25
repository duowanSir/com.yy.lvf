package com.yy.lvf.camera;

import com.yy.lvf.LLog;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

public class CameraActivity3 extends Activity {
	private static final String	TAG	= CameraActivity3.class.getSimpleName();
	private SurfaceView			mPreviewSv;
	private TextureView			mPreviewTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity3);

		mPreviewSv = (SurfaceView) findViewById(R.id.preview_sv0);
		mPreviewTv = (TextureView) findViewById(R.id.preview_tv);

		mPreviewSv.getHolder().addCallback(new Callback() {

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				LLog.d(TAG, "surfaceCreated(" + holder + ")");
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				LLog.d(TAG, "surfaceDestroyed(" + holder + ")");
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				LLog.d(TAG, "surfaceChanged(" + holder + ", " + format + ", " + width + ", " + height + ")");
			}
		});

		mPreviewTv.setSurfaceTextureListener(new SurfaceTextureListener() {
			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
				LLog.d(TAG, "onSurfaceTextureAvailable(" + surface + ", " + width + ", " + height + ")");
			}

			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
				LLog.d(TAG, "onSurfaceTextureDestroyed(" + surface + ")");
				return false;
			}

			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture surface) {
				LLog.d(TAG, "onSurfaceTextureUpdated(" + surface + ")");
			}

			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
				LLog.d(TAG, "onSurfaceTextureSizeChanged(" + surface + ", " + width + ", " + height + ")");
			}

		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		LLog.d(TAG, "onResume()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		LLog.d(TAG, "onPause()");
	}
}
