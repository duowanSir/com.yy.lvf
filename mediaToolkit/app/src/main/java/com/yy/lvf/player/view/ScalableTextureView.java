package com.yy.lvf.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by slowergun on 2016/11/22.
 */
public class ScalableTextureView extends TextureView {

    public enum ScaleType {
        CENTER_INSIDE, CENTER_CROP, FIT_CENTER, FIT_X_START
    }

    public static final String TAG = ScalableTextureView.class.getSimpleName();
    private Integer mContentWidth;
    private Integer mContentHeight;
    private Integer mContentOrientation = 0;
    private int mMeasureWidth;
    private int mMeasureHeight;
    private ScaleType mScaleType;
    private float mPivotX;
    private float mPivotY;
    private float mRotation;

    public ScalableTextureView(Context context) {
        this(context, null);
    }

    public ScalableTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScalableTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setContentOrientation(Integer orientation) {
        mContentOrientation = orientation;
    }

    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
    }

    public void setContentSize(int width, int height) {
        if (mContentOrientation == 90 || mContentOrientation == 270) {
            mContentWidth = height;
            mContentHeight = width;
            return;
        }
        mContentWidth = width;
        mContentHeight = height;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasureWidth = MeasureSpec.getSize(widthMeasureSpec);
        mMeasureHeight = MeasureSpec.getMode(heightMeasureSpec);
        int wm = MeasureSpec.getMode(widthMeasureSpec);
        int hm = MeasureSpec.getMode(heightMeasureSpec);
        if (mContentWidth != null && mContentWidth != null) {
            adaptSize(wm, hm);
        }
    }

    private void adaptSize(final int widthMode, final int heightMode) {
        if (mContentWidth == 0 || mContentHeight == 0) {
            throw new RuntimeException("invalid content size");
        }
        if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("invalid measure mode");
        }
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            switch (mScaleType) {
                case CENTER_INSIDE:
                    mPivotX = mMeasureWidth >> 1;
                    mPivotY = mMeasureHeight >> 1;
                    if (mContentWidth > mMeasureWidth) {
                        float ratio = (float) mContentHeight / mContentWidth;
                        mContentWidth = mMeasureWidth;
                        mContentHeight = (int) (mContentWidth * ratio);
                        if (mContentHeight > mMeasureHeight) {
                            mContentHeight = mMeasureHeight;
                            mContentWidth = (int) (mContentHeight / ratio);
                        }
                        break;
                    }
                    if (mContentHeight > mMeasureHeight) {
                        float ratio = (float) mContentHeight / mContentWidth;
                        mContentHeight = mMeasureHeight;
                        mContentWidth = (int) (mContentHeight / ratio);
                        if (mContentWidth > mMeasureWidth) {
                            mContentWidth = mMeasureWidth;
                            mContentHeight = (int) (mContentWidth * ratio);
                        }
                    }
                    break;
                case CENTER_CROP:
                    mPivotX = mMeasureWidth >> 1;
                    mPivotY = mMeasureHeight >> 1;
                    float ratio = (float) mContentHeight / mContentWidth;
                    mContentWidth = mMeasureWidth;
                    mContentHeight = (int) (mContentWidth * ratio);
                    if (mContentHeight < mMeasureHeight) {
                        mContentHeight = mMeasureHeight;
                        mContentWidth = (int) (mContentHeight / ratio);
                    }
                    break;
                case FIT_CENTER:
                    mPivotX = mMeasureWidth >> 1;
                    mPivotY = mMeasureHeight >> 1;
                    if (mContentWidth != mMeasureWidth && mContentHeight != mMeasureHeight) {
                        float ratio1 = (float) mContentHeight / mContentWidth;
                        mContentWidth = mMeasureWidth;
                        mContentHeight = (int) (mContentWidth / ratio1);
                        if (mContentHeight > mMeasureHeight) {
                            mContentHeight = mMeasureHeight;
                            mContentWidth = (int) (mContentHeight / ratio1);
                        }
                    }
                    break;
                case FIT_X_START:
                    if (mContentWidth != mMeasureWidth) {
                        float ratio1 = (float) mContentHeight / mContentWidth;
                        mContentWidth = mMeasureWidth;
                        mContentHeight = (int) (mContentWidth / ratio1);
                    }
                    mPivotX = mContentWidth >> 1;
                    mPivotY = mContentHeight >> 1;
                    break;
                default:
                    throw new RuntimeException("unsupport scaleType param");
            }
        }
    }

}
