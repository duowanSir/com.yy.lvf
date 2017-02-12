package com.android.lvf.demo.surface.entity;

import android.hardware.Camera;

/**
 * Created by slowergun on 2017/1/7.
 */
public enum CameraFacing {
    BACK(Camera.CameraInfo.CAMERA_FACING_BACK), FRONT(Camera.CameraInfo.CAMERA_FACING_FRONT);
    public int mFacing;

    CameraFacing(int facing) {
        mFacing = facing;
    }

    public int getFacing() {
        return mFacing;
    }

    public void setFacing(int mFacing) {
        this.mFacing = mFacing;
    }
}
