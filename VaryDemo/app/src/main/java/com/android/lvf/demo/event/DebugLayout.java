package com.android.lvf.demo.event;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.android.lvf.LLog;

/**
 * Created by slowergun on 2016/12/8.
 */
public class DebugLayout extends LinearLayout {
    public static final String TAG = DebugLayout.class.getSimpleName();
    private int mDp10;

    public DebugLayout(Context context) {
        this(context, null, 0);
    }

    public DebugLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DebugLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDp10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private float   mDownX;
    private float   mDownY;
    private boolean mIsVerticalScroll;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                LLog.d(TAG, "ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                int distanceX = (int) (ev.getX() - mDownX);
                int distanceY = (int) (ev.getY() - mDownY);
                distanceX = Math.abs(distanceX);
                distanceY = Math.abs(distanceY);
                if (distanceY > distanceX && distanceY >= mDp10) {
                    mIsVerticalScroll = true;
                } else {
                    mIsVerticalScroll = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                LLog.d(TAG, "ACTION_UP");
                break;
        }
        if (mIsVerticalScroll) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LLog.d(TAG, "ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                LLog.d(TAG, "ACTION_UP");
                break;
        }
        if (mIsVerticalScroll) {
            return false;
        } else {
            return true;
        }
    }
}
