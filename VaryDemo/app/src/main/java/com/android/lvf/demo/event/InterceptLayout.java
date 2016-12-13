package com.android.lvf.demo.event;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.android.lvf.LLog;

/**
 * Created by slowergun on 2016/12/8.
 */
public class InterceptLayout extends FrameLayout {
    public InterceptLayout(Context context) {
        super(context);
    }

    public InterceptLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean done = super.onTouchEvent(event);
        LLog.d(ListCoverPlayActivity.TAG, "onTouchEvent(" + event.getAction() + ", " + done + ")");
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean done = super.onInterceptTouchEvent(event);
        LLog.d(ListCoverPlayActivity.TAG, "onInterceptTouchEvent(" + event.getAction() + ", " + done + ")");
        return done;
    }

}
