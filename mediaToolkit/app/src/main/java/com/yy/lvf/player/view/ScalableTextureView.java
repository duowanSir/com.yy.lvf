package com.yy.lvf.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.yy.lvf.LLog;

/**
 * Created by slowergun on 2016/11/22.
 */
public class ScalableTextureView extends TextureView {

    public enum ScaleType {
        CENTER_INSIDE, CENTER_CROP, FIT_CENTER, FIT_X_START
    }

    public static final String TAG = ScalableTextureView.class.getSimpleName();
    protected Integer mContentWidth;
    protected Integer mContentHeight;
    protected int     mMeasureWidth;
    protected int     mMeasureHeight;
    private Integer mContentOrientation = 0;
    private ScaleType mScaleType;
    private float     mPivotX;
    private float     mPivotY;
    private float     mRotation;

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
        mMeasureWidth = MeasureSpec.getSize(widthMeasureSpec);
        mMeasureHeight = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        LLog.d(TAG, "measureSpec[" + getModeLog(widthMode) + ", " + mMeasureWidth + ", " + getModeLog(heightMode) + ", " + mMeasureHeight + "]");
        if (mContentWidth != null && mContentWidth != null) {
            adaptSize(widthMode, heightMode);
            setMeasuredDimension(mMeasureWidth, mMeasureHeight);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void adaptSize(final int widthMode, final int heightMode) {
        float ratio = 0;
        switch (mScaleType) {
            case CENTER_INSIDE:
                if (mContentWidth > mMeasureWidth && mMeasureWidth > 0) {
                    if (ratio == 0) {
                        ratio = (float) mContentHeight / mContentWidth;
                    }
                    mContentWidth = mMeasureWidth;
                    mContentHeight = (int) (mContentWidth * ratio);
                }
                if (mContentHeight > mMeasureHeight && mMeasureHeight > 0) {
                    if (ratio == 0) {
                        ratio = (float) mContentHeight / mContentWidth;
                    }
                    mContentHeight = mMeasureHeight;
                    mContentWidth = (int) (mContentHeight / ratio);
                }
                if (widthMode != MeasureSpec.EXACTLY) {
                    mMeasureWidth = mContentWidth;
                }
                if (heightMode != MeasureSpec.EXACTLY) {
                    mMeasureHeight = mContentHeight;
                }
                mPivotX = mMeasureWidth >> 1;
                mPivotY = mMeasureHeight >> 1;
                break;
            case CENTER_CROP:
                if (mContentWidth != mMeasureWidth && mMeasureWidth > 0) {
                    if (widthMode == MeasureSpec.EXACTLY || mContentWidth > mMeasureWidth) {
                        if (ratio == 0) {
                            ratio = (float) mContentHeight / mContentWidth;
                        }
                        mContentWidth = mMeasureWidth;
                        mContentHeight = (int) (mContentWidth * ratio);
                    }
                }
                if (mContentHeight < mMeasureHeight && mMeasureHeight > 0) {
                    if (widthMode == MeasureSpec.EXACTLY) {
                        if (ratio == 0) {
                            ratio = (float) mContentHeight / mContentWidth;
                        }
                        mContentHeight = mMeasureHeight;
                        mContentWidth = (int) (mContentHeight / ratio);
                    }
                }
                if (widthMode != MeasureSpec.EXACTLY) {
                    mMeasureWidth = mContentWidth;
                }
                if (heightMode != MeasureSpec.EXACTLY) {
                    mMeasureHeight = mContentHeight;
                }
                mPivotX = mMeasureWidth >> 1;
                mPivotY = mMeasureHeight >> 1;
                break;
            case FIT_CENTER:
                if (mContentWidth != mMeasureWidth && mMeasureWidth > 0) {
                    if (widthMode == MeasureSpec.EXACTLY || mContentWidth > mMeasureWidth) {
                        if (ratio == 0) {
                            ratio = (float) mContentHeight / mContentWidth;
                        }
                        mContentWidth = mMeasureWidth;
                        mContentHeight = (int) (mContentWidth * ratio);
                    }
                }
                if (mContentHeight > mMeasureHeight && mMeasureHeight > 0) {
                    if (widthMode == MeasureSpec.EXACTLY) {
                        if (ratio == 0) {
                            ratio = (float) mContentHeight / mContentWidth;
                        }
                        mContentHeight = mMeasureHeight;
                        mContentWidth = (int) (mContentHeight / ratio);
                    }
                }
                if (widthMode != MeasureSpec.EXACTLY) {
                    mMeasureWidth = mContentWidth;
                }
                if (heightMode != MeasureSpec.EXACTLY) {
                    mMeasureHeight = mContentHeight;
                }
                mPivotX = mMeasureWidth >> 1;
                mPivotY = mMeasureHeight >> 1;
                break;
            case FIT_X_START:
                if (mContentWidth != mMeasureWidth && mMeasureWidth > 0) {
                    if (widthMode == MeasureSpec.EXACTLY || mContentWidth > mMeasureWidth) {
                        if (ratio == 0) {
                            ratio = (float) mContentHeight / mContentWidth;
                        }
                        mContentWidth = mMeasureWidth;
                        mContentHeight = (int) (mContentWidth * ratio);
                    }
                }
                if (widthMode != MeasureSpec.EXACTLY) {
                    mMeasureWidth = mContentWidth;
                }
                if (heightMode != MeasureSpec.EXACTLY) {
                    mMeasureHeight = mContentHeight;
                }
                mPivotX = mMeasureWidth >> 1;
                mPivotY = mContentHeight >> 1;
                break;
            default:
                throw new RuntimeException("invalid scale type");
        }
    }

    private String getModeLog(int mode) {
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return "EXACTLY";
            case MeasureSpec.AT_MOST:
                return "AT_MOST";
            case MeasureSpec.UNSPECIFIED:
                return "UNSPECIFIED";
            default:
                return "UNKNOWN";
        }
    }

}
