package com.android.lvf.demo.surface;

import android.app.Activity;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Process;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.android.lvf.R;

/**
 * Created by slowergun on 2016/12/26.
 */
public class ActivitySurfaceCanvasUse extends Activity {
    private SurfaceView mSurfaceView;

    private HandlerThread        mRenderThread;
    private SurfaceRenderHandler mRenderHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_canvas_use);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mRenderHandler.msgDraw(25, 11);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mRenderHandler.removeCallbacksAndMessages(null);
            }
        });

        mRenderThread = new HandlerThread("animation_thread", Process.THREAD_PRIORITY_BACKGROUND);
        mRenderThread.start();

        mRenderHandler = new SurfaceRenderHandler(mRenderThread.getLooper(), mSurfaceView.getHolder());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRenderThread.quit();
    }
}
