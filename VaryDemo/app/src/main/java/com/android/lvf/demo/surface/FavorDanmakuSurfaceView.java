package com.android.lvf.demo.surface;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.android.lvf.R;
import com.android.lvf.demo.surface.entity.BitmapDanmaku;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by slowergun on 2017/1/5.
 */
public class FavorDanmakuSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private Context       mContext;
    private HandlerThread mRenderThread;
    private RenderHandler mRenderHandler;

    public FavorDanmakuSurfaceView(Context context) {
        this(context, null);
    }

    public FavorDanmakuSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FavorDanmakuSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mRenderThread = new HandlerThread("favor_animation_renderer", Process.THREAD_PRIORITY_BACKGROUND);

        setZOrderOnTop(true);
        setWillNotCacheDrawing(true);
        setDrawingCacheEnabled(false);
        setWillNotDraw(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mRenderThread.start();
        mRenderHandler = new RenderHandler(mContext, mRenderThread.getLooper(), holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mRenderHandler.removeCallbacksAndMessages(null);
        mRenderHandler = null;
        mRenderThread.quit();
    }

    public void favor(int fps, long duration) {
        if (mRenderHandler == null) {
            return;
        }
        mRenderHandler.msgFavor(fps, duration);
    }

    public class RenderHandler extends Handler {
        private static final int MSG_FAVOR = 0;
        private static final int MSG_DRAW  = 1;
        private Context       mContext;
        private SurfaceHolder mSurfaceHolder;
        private int[] mCenterDanmakuArray = {R.mipmap.danmaku_8, R.mipmap.danmaku, R.mipmap.danmaku_9, R.mipmap.danmaku_5};
        private int[] mOtherDanmakuArray  = {R.mipmap.danmaku_1, R.mipmap.danmaku_2, R.mipmap.danmaku_3, R.mipmap.danmaku_4, R.mipmap.danmaku_6, R.mipmap.danmaku_7, R.mipmap.danmaku_10};

        private LinkedList<BitmapDanmaku> mProceed = new LinkedList<>();

        private int     mFps;
        private long    mDurationMs;
        private long    mDelayMs;
        private boolean mFinished = true;

        public RenderHandler(Context context, Looper looper, SurfaceHolder surfaceHolder) {
            super(looper);
            mContext = context;
            mSurfaceHolder = surfaceHolder;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FAVOR:
                    handleFavor(msg);
                    break;
                case MSG_DRAW:
                    handleDraw();
                    break;
                default:
                    break;
            }
        }

        public void msgFavor(int fps, long duration) {
            Message msg = obtainMessage(MSG_FAVOR);
            msg.arg1 = fps;
            msg.obj = duration;
            sendMessage(msg);
        }

        private void msgDraw(long delay) {
            sendEmptyMessageDelayed(MSG_DRAW, delay);
        }

        private void handleFavor(Message msg) {
            if (!mFinished) {
                return;
            }
            mFps = msg.arg1;
            mDurationMs = (long) msg.obj;
            mDelayMs = 1000 / mFps;
            if (mDelayMs <= 0) {
                return;
            }
            mFinished = false;
            initDanmaku();
            handleDraw();
        }

        private void initDanmaku() {
            Random centerRandom = new Random(mCenterDanmakuArray.length);
            Random otherRandom = new Random(mOtherDanmakuArray.length);
            Resources res = mContext.getResources();
            mProceed.clear();
            for (int i = 0; i < 8; i++) {
                if (i >= 2 && i <= 5) {
                    Bitmap bmp = BitmapFactory.decodeResource(res, mCenterDanmakuArray[Math.abs(centerRandom.nextInt()) % mCenterDanmakuArray.length]);
                    BitmapDanmaku bd = new BitmapDanmaku(bmp, 100 * i, 200 * i, 100 * i, BitmapDanmaku.MAX_Y);
                    bd.setYSpeed(mFps, mDurationMs);
                    mProceed.add(bd);
                } else {
                    Bitmap bmp = BitmapFactory.decodeResource(res, mOtherDanmakuArray[Math.abs(otherRandom.nextInt()) % mOtherDanmakuArray.length]);
                    BitmapDanmaku bd = new BitmapDanmaku(bmp, 100 * i, 200 * i, 100 * i, BitmapDanmaku.MAX_Y);
                    bd.setYSpeed(mFps, mDurationMs);
                    mProceed.add(bd);
                }
            }
        }

        private void handleDraw() {
            synchronized (mSurfaceHolder) {
                if (mFinished) {
                    Canvas canvas = mSurfaceHolder.lockCanvas();
                    canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                    return;
                }
                mFinished = true;
                for (BitmapDanmaku each : mProceed) {
                    if (each.mYCurrent < each.mYEnd) {
                        mFinished = false;
                        break;
                    }
                }
                Canvas canvas = mSurfaceHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
                for (int i = 0; i < mProceed.size(); i++) {
                    BitmapDanmaku each = mProceed.get(i);
                    canvas.drawBitmap(each.mBitmap, each.mXCurrent, each.mYCurrent, null);
                    each.mXCurrent += each.mXSpeed;
                    each.mYCurrent += each.mYSpeed;
                }
                mSurfaceHolder.unlockCanvasAndPost(canvas);
                msgDraw(mDelayMs);
            }
        }
    }
}
