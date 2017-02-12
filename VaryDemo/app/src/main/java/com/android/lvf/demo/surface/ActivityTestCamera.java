package com.android.lvf.demo.surface;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.lvf.R;
import com.android.lvf.demo.surface.entity.CameraFacing;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by slowergun on 2017/1/7.
 */
public class ActivityTestCamera extends Activity implements SurfaceHolder.Callback,
        View.OnClickListener,
        Camera.ShutterCallback,
        Camera.PictureCallback {
    private Camera mCamera;
    private int    mCameraId;
    private CameraFacing mFacing = CameraFacing.BACK;
    private SurfaceView mCameraSurfaceView;
    private Button      mSwitchBtn;
    private Button      mTakePictureBtn;

    private boolean mSurfaceAlive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_camera);

        mCameraSurfaceView = (SurfaceView) findViewById(R.id.camera_surface_view);
        mSwitchBtn = (Button) findViewById(R.id.switch_btn);
        mTakePictureBtn = (Button) findViewById(R.id.take_picture_btn);

        mCameraSurfaceView.getHolder().addCallback(this);
        mSwitchBtn.setOnClickListener(this);
        mTakePictureBtn.setOnClickListener(this);

        getBaseContext();
        getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCameraId = getCameraId(mFacing);
            mCamera = Camera.open(mCameraId);
        }
        if (mCamera != null && mSurfaceAlive) {
            try {
                mCamera.setPreviewDisplay(mCameraSurfaceView.getHolder());
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            mCameraId = -1;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceAlive = true;
        if (mCamera != null && mSurfaceAlive) {
            try {
                mCamera.setPreviewDisplay(mCameraSurfaceView.getHolder());
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceAlive = false;

        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public static int getCameraId(CameraFacing facing) {
        int cameraNum = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraNum; i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing.getFacing()) {
                return i;
            }
        }
        return -1;
    }

    private int mPictureNum = 0;

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File file = new File(dir, (mPictureNum++) + ".JPG");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (data != null) {
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(file));
                bos.write(data,0, data.length);
                bos.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                        bos = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (mCamera != null && mSurfaceAlive) {
            try {
                mCamera.setPreviewDisplay(mCameraSurfaceView.getHolder());
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onShutter() {
        Toast.makeText(this, "shutter", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (v == mSwitchBtn) {
            if (mFacing == CameraFacing.BACK) {
                mFacing = CameraFacing.FRONT;
            } else {
                mFacing = CameraFacing.BACK;
            }
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
                mCameraId = -1;
            }
            if (mCamera == null) {
                mCameraId = getCameraId(mFacing);
                mCamera = Camera.open(mCameraId);
            }
            if (mCamera != null && mSurfaceAlive) {
                try {
                    mCamera.setPreviewDisplay(mCameraSurfaceView.getHolder());
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == mTakePictureBtn) {
            if (mCamera != null && mSurfaceAlive) {
                mCamera.takePicture(this, this, this, this);
            }
        }
    }
}
