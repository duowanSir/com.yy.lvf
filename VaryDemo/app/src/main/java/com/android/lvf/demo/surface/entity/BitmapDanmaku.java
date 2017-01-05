package com.android.lvf.demo.surface.entity;

import android.graphics.Bitmap;

/**
 * Created by slowergun on 2016/12/27.
 */
public class BitmapDanmaku {
    public static int MAX_X;
    public static int MAX_Y;

    public Bitmap mBitmap;
    /**
     * 起始坐标
     */
    public float  mXStart;
    public float  mYStart;
    /**起始坐标*/
    /**
     * 终止坐标
     */
    public float  mXEnd;
    public float  mYEnd;
    /**终止坐标*/
    /**
     * 变化坐标
     */
    public float  mXCurrent;
    public float  mYCurrent;
    /**
     * 变化坐标
     */
    public float  mYSpeed;
    public float  mXSpeed;

    public BitmapDanmaku(Bitmap bmp, float xStart, float yStart, float xEnd, float yEnd) {
        mBitmap = bmp;
        mXStart = xStart;
        mYStart = yStart;
        mXEnd = xEnd;
        mYEnd = yEnd;

        mXCurrent = xStart;
        mYCurrent = yStart;
    }

    public boolean setYSpeed(int fps, long duration) {
        int frames = (int) (fps * (duration / 1000f));
        if (frames < 1) {
            return false;
        }
        mXSpeed = (mXEnd - mYEnd) / (float) frames;
        mYSpeed = (mYEnd - mYStart) / (float) frames;
        return true;
    }
}
