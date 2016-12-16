package com.android.lvf.demo.animation;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.android.lvf.LLog;
import com.android.lvf.R;

/**
 * Created by 烽 on 2016/12/15.
 */

public class ActivityPropertyAnimation extends Activity implements View.OnClickListener {
    private static final String TAG = ActivityPropertyAnimation.class.getSimpleName();
    private View mSrcV;
    private View mDstV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_animation);

        mSrcV = findViewById(R.id.src_view);
        mDstV = findViewById(R.id.dst_view);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            LLog.d(TAG, "dst:[" + mDstV.getWidth() + ", " + mDstV.getHeight() + ", " + mDstV.getTop() + "]");
            LLog.d(TAG, "src:[" + mSrcV.getWidth() + ", " + mSrcV.getHeight() + ", " + mSrcV.getTop() + "]");

//            mSrcV.setPivotX(mSrcV.getWidth() / 2);
//            mSrcV.setPivotY(mSrcV.getHeight() / 2);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_btn) {
            mSrcV.setPivotX(0);
            mSrcV.setPivotY(0);
            objectAnimation1();
        }
    }

    private void objectAnimation() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mSrcV, "unknown", 0f, 6f)
                .setDuration(2000);
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        // ?AnimatorSet
        // ?Frame refresh delay
        // ?AnimationInflater
        // ?TypeEvaluator
        // ?PropertyValuesHolder
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationPause(Animator animation) {
                super.onAnimationPause(animation);
            }

            @Override
            public void onAnimationResume(Animator animation) {
                super.onAnimationResume(animation);
            }
        });
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mSrcV.setScaleX(value);
                mSrcV.setScaleY(value);
            }
        });
        objectAnimator.start();
    }

    private void objectAnimation1() {
        PropertyValuesHolder propertyScaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 6f);
        PropertyValuesHolder propertyScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 6f);
        PropertyValuesHolder propertyTransitionY = PropertyValuesHolder.ofFloat("translationY", mSrcV.getTop(), mDstV.getTop());
        final ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mSrcV, propertyScaleX, propertyScaleY, propertyTransitionY)
                .setDuration(1000);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        // ?AnimatorSet
        // ?Frame refresh delay
        // ?AnimationInflater
        // ?TypeEvaluator
        // ?PropertyValuesHolder
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ValueAnimator valueAnimator = (ValueAnimator) animation;
                float scaleX = (float) valueAnimator.getAnimatedValue("scaleX");
                float scaleY = (float) valueAnimator.getAnimatedValue("scaleY");
                float translationY = (float) valueAnimator.getAnimatedValue("translationY");
                LLog.d(TAG, "value:[" + scaleX + ", " + scaleY + ", " + translationY + "]");
                LLog.d(TAG, "src:[" + mSrcV.getWidth() + ", " + mSrcV.getHeight() + ", " + mSrcV.getTop() + "]");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationPause(Animator animation) {
                super.onAnimationPause(animation);
            }

            @Override
            public void onAnimationResume(Animator animation) {
                super.onAnimationResume(animation);
            }
        });
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
            }
        });
        objectAnimator.start();
    }
}
