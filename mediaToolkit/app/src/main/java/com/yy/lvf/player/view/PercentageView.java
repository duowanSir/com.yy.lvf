package com.yy.lvf.player.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by slowergun on 2016/10/31.
 */
public class PercentageView extends TextView {
    private RotateDrawable mLoadingDrawable;
    private boolean mLoading = false;
    private int mLevel = 0;
    private Runnable mLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLoading) {
                mLoadingDrawable.setLevel(mLevel);
                mLevel += 2;
                postDelayed(this, 250);
            }
        }
    };

    public PercentageView(Context context) {
        this(context, null);
    }

    public PercentageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
        if (background instanceof RotateDrawable) {
            mLoadingDrawable = (RotateDrawable) background;
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            start();
        } else {
            stop();
        }
    }

    private void start() {
        if (mLoadingDrawable != null) {
            mLoading = true;
            mLevel = 0;
            post(mLoadingRunnable);
        }
    }

    private void stop() {
        removeCallbacks(mLoadingRunnable);
        mLoading = false;
    }
}
