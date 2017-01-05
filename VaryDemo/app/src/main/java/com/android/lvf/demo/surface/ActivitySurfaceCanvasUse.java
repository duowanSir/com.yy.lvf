package com.android.lvf.demo.surface;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.android.lvf.R;
import com.android.lvf.demo.surface.entity.BitmapDanmaku;

import java.util.LinkedList;

/**
 * Created by slowergun on 2016/12/26.
 */
public class ActivitySurfaceCanvasUse extends Activity  implements View.OnClickListener {
    private FavorDanmakuSurfaceView mFavorDanmakuSurfaceView;
    private Button mFavorBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_canvas_use);
        mFavorDanmakuSurfaceView = (FavorDanmakuSurfaceView) findViewById(R.id.favor_danmaku_surface_view);
        mFavorBtn = (Button) findViewById(R.id.favor_btn);

        mFavorBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mFavorBtn) {
            mFavorDanmakuSurfaceView.favor(15, 500);
        }
    }
}
