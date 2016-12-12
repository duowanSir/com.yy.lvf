package com.android.lvf;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by slowergun on 2016/12/2.
 */
public class TestSurfaceViewAndTextureView extends Activity implements View.OnClickListener,
        SurfaceHolder.Callback,
        TextureView.SurfaceTextureListener {
    public static final String TAG = TestSurfaceViewAndTextureView.class.getSimpleName();
    private SurfaceView mSurfaceView;
    private TextureView mTextureView;

    private FrameLayout mNewLayout;
    private FrameLayout mOldLayout;
    private FrameLayout mOldLayout1;

    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_surface_view_and_texture_view);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mTextureView = (TextureView) findViewById(R.id.texture_view);
        mNewLayout = (FrameLayout) findViewById(R.id.new_layout);
        mOldLayout = (FrameLayout) findViewById(R.id.old_layout);
        mOldLayout1 = (FrameLayout) findViewById(R.id.old_layout1);

        mSurfaceView.getHolder().addCallback(this);
        mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.release();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.old_layout) {
            if (mNewLayout.getChildCount() != 0) {
                Toast.makeText(this, "已经有预览存在", Toast.LENGTH_SHORT).show();
                return;
            }
            mOldLayout.removeView(mSurfaceView);
            mNewLayout.addView(mSurfaceView);
        } else if (v.getId() == R.id.old_layout1) {
            if (mNewLayout.getChildCount() != 0) {
                Toast.makeText(this, "已经有预览存在", Toast.LENGTH_SHORT).show();
                return;
            }
            mOldLayout1.removeView(mTextureView);
            mNewLayout.addView(mTextureView);
        } else if (v.getId() == R.id.new_layout) {
            if (mOldLayout.getChildCount() == 0) {
                mNewLayout.removeView(mSurfaceView);
                mOldLayout.addView(mSurfaceView);
            } else if (mOldLayout1.getChildCount() == 0) {
                mNewLayout.removeView(mTextureView);
                mOldLayout1.addView(mTextureView);
            }
        } else {
            throw new RuntimeException("unimplemented click");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated(" + holder + ", " + Thread.currentThread() + ")");

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged(" + holder + ", " + format + ", " + width + ", " + height + ", " + Thread.currentThread() + ")");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed(" + holder + ", " + Thread.currentThread() + ")");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable(" + surface + ", " + width + ", " + height + ", " + Thread.currentThread() + ")");
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureSizeChanged(" + surface + ", " + width + ", " + height + ", " + Thread.currentThread() + ")");
        mCamera.stopPreview();
        mCamera.startPreview();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed(" + surface + ", " + Thread.currentThread() + ")");
        try {
            mCamera.setPreviewTexture(null);
            mCamera.stopPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        Log.d(TAG, "onSurfaceTextureUpdated(" + surface + ", " + Thread.currentThread() + ")");
    }
}
