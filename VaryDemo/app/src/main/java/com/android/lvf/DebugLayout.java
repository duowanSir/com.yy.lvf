package com.android.lvf;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by slowergun on 2016/12/8.
 */
public class DebugLayout extends LinearLayout {
    private GestureDetector mGestureDetector;

    public DebugLayout(Context context) {
        super(context);
    }

    public DebugLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DebugLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    float x, y;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean done = super.onTouchEvent(event);
        if (mGestureDetector != null) {
            done = mGestureDetector.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                done = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - x) >= Math.abs(event.getY() - y)) {
                    done = true;
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    done = false;
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
        }
        LLog.d(ListCoverPlayActivity.TAG, "onTouchEvent(" + event.getAction() + ", " + done + ")");
        return done;
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//        boolean done = super.onInterceptTouchEvent(event);
//        LLog.d(ListCoverPlayActivity.TAG, "onInterceptTouchEvent(" + event.getAction() + ", " + done + ")");
//        return done;
//    }

    public void setGestureDetector(GestureDetector detector) {
//        mGestureDetector = detector;
    }

}
