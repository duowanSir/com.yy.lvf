package com.yy.lvf.player.demo;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;

/**
 * Created by slowergun on 2016/11/4.
 */
public class CoordinateActivity extends Activity {
    private int mScreenWidth;
    private int mScreenHeight;

    private int mStatusBarWidth;
    private int mStatusBarHeight;

    private int mDecorViewWidth;
    private int mDecorViewHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            mScreenWidth = dm.widthPixels;
            mScreenHeight = dm.heightPixels;

            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

            Rect rect1 = new Rect();
            getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(rect1);

        }
    }
}
