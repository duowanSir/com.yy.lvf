package com.android.lvf.demo.surface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;

/**
 * Created by slowergun on 2016/12/26.
 */
public class SurfaceRenderHandler extends Handler {
    private static final int MSG_DRAW = 0;
    private SurfaceHolder mSurfaceHolder;

    private Paint mPaint;

    public SurfaceRenderHandler(Looper looper, SurfaceHolder surfaceHolder) {
        super(looper);
        mSurfaceHolder = surfaceHolder;

        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(10);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_DRAW:
                handleDraw(msg.arg1, msg.arg2);
                break;
            default:
                break;
        }
    }

    public void msgDraw(int fps, int frames) {
        Message msg = obtainMessage(MSG_DRAW);
        msg.arg1 = fps;
        msg.arg2 = frames;
        sendMessage(msg);
    }

    private float x = 50, x1 = 150;
    private float y, y1;
    private float h = 200, h1 = 500;

    private void handleDraw(int fps, int frames) {
        synchronized (mSurfaceHolder) {
            Canvas canvas = mSurfaceHolder.lockCanvas();

            if (y >= 1760 && y1 >= 1760) {
                y = 0;
                y1 = 0;
            }

            canvas.drawColor(Color.BLACK);
            canvas.drawLine(x, y, x, y + h, mPaint);
            canvas.drawLine(x1, y1, x1, y1 + h1, mPaint);

            y += 20;
            y1 += 50;
            mSurfaceHolder.unlockCanvasAndPost(canvas);

            if (fps > 0) {
                if (fps >= 100) {
                    fps = 100;
                }
                if (fps <= 0) {
                    fps = 1;
                }
                int duration = 1000 / fps;
                Message msg = obtainMessage(MSG_DRAW);
                msg.arg1 = fps;
                msg.arg2 = frames;
                sendMessageDelayed(msg, duration);
//                msgDraw(duration, frames);
            }
        }
    }
}
