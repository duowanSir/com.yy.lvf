package com.android.lvf.player.demo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.lvf.LLog;
import com.android.lvf.R;
import com.android.lvf.player.IFullScreen;
import com.android.lvf.player.view.PercentageView;
import com.ycloud.playersdk.BasePlayer;
import com.ycloud.playersdk.YYTexTurePlayer;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by slowergun on 2016/10/26.
 */
public class YYMediaController extends FrameLayout implements
        View.OnClickListener,
        View.OnTouchListener,
        SeekBar.OnSeekBarChangeListener,
        BasePlayer.OnMessageListener,
        IFullScreen {
    public static class UiHandler extends Handler {
        private WeakReference<YYMediaController> mController;

        public UiHandler(YYMediaController controller) {
            mController = new WeakReference<>(controller);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mController.get() == null) {
                return;
            }
            switch (msg.what) {
                case SHOW_PROGRESS:
                    int pos = mController.get().setProgress();
                    if (!mController.get().mDragging && mController.get().mShowing && mController.get().mMediaPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                case FADE_OUT:
                    mController.get().hide();
                    break;
                case BUFFERING_START:
                    if (mController.get().mLoadingView.getVisibility() == View.GONE) {
                        mController.get().mLoadingView.setText("");
                        mController.get().mLoadingView.setVisibility(View.VISIBLE);
                    }
                    break;
                case BUFFERING_END:
                    if (mController.get().mLoadingView.getVisibility() == View.VISIBLE) {
                        mController.get().mLoadingView.setVisibility(View.GONE);
                        mController.get().mLoadingView.setText("");
                    }
                    break;
                case ERROR:
                    mController.get().mMediaPlayerCreated = false;
                    mController.get().mLoadingView.setVisibility(View.GONE);
                    mController.get().updatePausePlay();
                    break;
                case VIDEO_RENDERING_START:
                case PREPARED:
                case COMPLETED:
                    mController.get().mLoadingView.setVisibility(View.GONE);
                    mController.get().updatePausePlay();
                    break;
                case UPDATE_PLAY_AND_PAUSE:
                    mController.get().updatePausePlay();
                    break;
            }
        }
    }

    public interface Callback {
        void onPrePlay();
    }

    public static final String TAG = YYMediaController.class.getSimpleName();

    private static final int DEFAULT_TIMEOUT       = Integer.MAX_VALUE;
    private static final int FADE_OUT              = 1;
    private static final int SHOW_PROGRESS         = 2;
    private static final int UPDATE_PLAY_AND_PAUSE = 4;
    private static final int PREPARED              = 8;
    private static final int ERROR                 = 16;
    private static final int COMPLETED             = 32;
    private static final int BUFFERING_START       = 64;
    private static final int BUFFERING_END         = 128;
    private static final int VIDEO_RENDERING_START = 256;

    private Context         mContext;
    private LayoutInflater  mInflater;
    private YYTexTurePlayer mMediaPlayer;
    private ViewGroup       mAnchorListWrapper;
    private ViewGroup       mAnchor;
    private Callback        mCallback;
    private UiHandler       mUiHandler;
    /**
     * remove
     */
    private LayoutParams    mLayout0Lp;
    private View            mLayout0;
    private ImageView       mPlayIv;
    private TextView        mPositionTv;
    private SeekBar         mProgressSb;
    private TextView        mDurationTv;
    private ImageView       mFullIv;

    private LayoutParams mReturnIvLp;
//    private ImageView mReturnIv;
    /**
     * remove
     */

    /**
     * gone
     */
    private ImageView      mCentralPlayIv;
    private PercentageView mLoadingView;
    /**
     * gone
     */

    private boolean mShowing            = false;
    private boolean mDragging           = false;// 是否在拖拽的过程中
    private boolean mIsFullScreen       = false;
    private boolean mMediaPlayerCreated = false;
    private boolean mOnResumed          = true;
    private StringBuilder mFormatBuilder;
    private Formatter     mFormatter;

    public YYMediaController(Context context) {
        this(context, null);
    }

    public YYMediaController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YYMediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);

        mUiHandler = new UiHandler(this);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        setOnTouchListener(this);

        init();
    }

    private void init() {
        setBackgroundColor(0x2f000000);
        initCentralPlayIv();
        initLoadingView();
        initLayout0();
//        initReturnIv();
        setOnTouchListener(this);
    }

    private void initCentralPlayIv() {
        mCentralPlayIv = new ImageView(mContext);
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int pixel125 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 62.5f, dm);
        LayoutParams lp = new LayoutParams(pixel125, pixel125);
        lp.gravity = Gravity.CENTER;
        mCentralPlayIv.setImageResource(R.drawable.play_video_selector);
        addView(mCentralPlayIv, lp);
        mCentralPlayIv.setOnClickListener(this);
    }

    private void initLoadingView() {
        mLoadingView = new PercentageView(mContext);
        mLoadingView.setGravity(Gravity.CENTER);
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int pixel125 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 62.5f, dm);
        LayoutParams lp = new LayoutParams(pixel125, pixel125);
        lp.gravity = Gravity.CENTER;
        mLoadingView.setBackgroundResource(R.drawable.media_controller_loading_drawable);
        mLoadingView.setVisibility(View.GONE);
        addView(mLoadingView, lp);
    }

    private void initLayout0() {
        mLayout0 = mInflater.inflate(R.layout.media_controller_layout0, this, false);
        mLayout0Lp = (LayoutParams) mLayout0.getLayoutParams();
        mLayout0Lp.gravity = Gravity.BOTTOM;

        mPlayIv = (ImageView) mLayout0.findViewById(R.id.media_controller_play_iv);
        mPositionTv = (TextView) mLayout0.findViewById(R.id.media_controller_position_tv);
        mProgressSb = (SeekBar) mLayout0.findViewById(R.id.media_controller_progress_sb);
        mProgressSb.setMax(1000);
        mDurationTv = (TextView) mLayout0.findViewById(R.id.media_controller_duration_tv);
        mFullIv = (ImageView) mLayout0.findViewById(R.id.media_controller_full_iv);

        mPlayIv.setOnClickListener(this);
        mProgressSb.setOnSeekBarChangeListener(this);
        mFullIv.setOnClickListener(this);
    }

//    private void initReturnIv() {
//        mReturnIv = new ImageView(mContext);
//        mReturnIv.setOnClickListener(this);
//
//        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
//        int pixel10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, dm);
//        int pixel140 = pixel10 * 14;
//        int pixel30 = pixel10 * 3;
//
//        mReturnIv.setPadding(pixel30, pixel30, pixel30, pixel30);
//        mReturnIv.setImageResource(R.drawable.media_controller_return_selector);
//
//        mReturnIvLp = new FrameLayout.LayoutParams(pixel140, pixel140);
//        mReturnIvLp.gravity = Gravity.TOP | Gravity.LEFT;
//    }

    public void reset() {
        mShowing = false;
        mDragging = false;
        mMediaPlayerCreated = false;

        mLoadingView.setVisibility(GONE);
        mCentralPlayIv.setVisibility(View.VISIBLE);
        mPlayIv.setImageResource(R.mipmap.media_controller_play_icon);
        mPositionTv.setText("");
        mProgressSb.setProgress(0);
        mDurationTv.setText("");

        removeView(mLayout0);
        mUiHandler.removeCallbacksAndMessages(null);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams lp2 = mAnchor.getLayoutParams();
                LLog.d(TAG, "[" + mAnchor + ", " + lp2.width + ", " + lp2.height + "]");
                ViewGroup.LayoutParams lp1 = YYMediaController.this.getLayoutParams();
                LLog.d(TAG, "[" + this + ", " + lp1.width + ", " + lp1.height + "]");
            }
        }, 2000);
    }

    private void removeUiHandlerMessage(int... whats) {
        if (mUiHandler == null || whats == null) {
            return;
        }
        for (int i : whats) {
            mUiHandler.removeMessages(i);
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void hide() {
        if (mShowing) {
            mUiHandler.removeMessages(SHOW_PROGRESS);
            // 官方使用mWindowManager.removeView(mDecor)实现隐藏功能
            removeView(mLayout0);
            mShowing = false;
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    public void setAnchorView(ViewGroup view) {
        if (!(view instanceof ViewGroup)) {
            throw new RuntimeException("anchor must be wrapper layout");
        }
        mAnchor = view;
        mAnchorListWrapper = (ViewGroup) mAnchor.getParent();
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mAnchor.addView(this, lp);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mLayout0.setEnabled(enabled);
    }

    public void setMediaPlayer(YYTexTurePlayer player) {
        mMediaPlayer = player;
    }

    public void show(int timeout) {
        if (!mShowing) {
            if (mLayout0.getParent() == null) {
                addView(mLayout0, mLayout0Lp);
            }
            setProgress();
            mShowing = true;
        }
        updatePausePlay();

        // 进度条要保持更新，例如暂停之后再点击播放。
        mUiHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mUiHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mUiHandler.removeMessages(FADE_OUT);
            mUiHandler.sendMessageDelayed(msg, timeout);
        }
    }

    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    public void showOnce(View view) {
    }

    private void updatePausePlay() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mPlayIv.setImageResource(R.mipmap.media_controller_pause_icon);
                mCentralPlayIv.setVisibility(GONE);
            } else {
                mPlayIv.setImageResource(R.mipmap.media_controller_play_icon);
                if (mLoadingView.getVisibility() == GONE) {
                    mCentralPlayIv.setVisibility(VISIBLE);
                }
            }
        }
    }

    public void doPauseResume() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pausePlay();
            updatePausePlay();
        } else {
            mMediaPlayer.play();
            mUiHandler.sendEmptyMessageDelayed(UPDATE_PLAY_AND_PAUSE, 500);
        }
    }

    private int setProgress() {
        if (mMediaPlayer == null || mDragging) {
            return 0;
        }
        int position = (int) mMediaPlayer.getTime();
        int duration = (int) mMediaPlayer.getLength();
        if (mProgressSb != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mProgressSb.setProgress((int) pos);
            }
        }

        if (mDurationTv != null)
            mDurationTv.setText(stringForTime(duration));
        if (mPositionTv != null)
            mPositionTv.setText(stringForTime(position));
        return position;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mMediaPlayerCreated) {
                if (mShowing) {
                    hide();
                } else {
                    show(Integer.MAX_VALUE);
                }
            } else {
                playBtnAction();
            }
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view == mPlayIv) {
            playBtnAction();
        } else if (view == mFullIv) {
            if (mContext instanceof IFullScreen.IFullScreenOwner) {
                ((IFullScreen.IFullScreenOwner) mContext).setFullScreenCallback(this);
            }
            int orientation = ((Activity) mContext).getWindowManager().getDefaultDisplay().getOrientation();
            if (orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_180) {
                ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                ((Activity) mContext).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        } else if (view == mCentralPlayIv) {
            mCentralPlayIv.setVisibility(GONE);
            playBtnAction();
        }
    }

    private void playBtnAction() {
        if (mMediaPlayerCreated) {
            doPauseResume();
        } else {
            if (mMediaPlayer != null) {
                throw new RuntimeException("old player need be release first");
            }
            YYPlayerProactivePlayingAdapter.Holder holder = (YYPlayerProactivePlayingAdapter.Holder) mAnchor.getTag();
            mMediaPlayer = new YYTexTurePlayer(mContext, null);
            holder.mYYTexturePlayer = mMediaPlayer;
            holder.mTextureView = (TextureView) mMediaPlayer.getView();
            mAnchor.setTag(holder);
            if (mAnchor instanceof FrameLayout) {
                LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                lp.gravity = Gravity.CENTER;
                mAnchor.addView(holder.mTextureView, 0, lp);
            } else if (mAnchor instanceof RelativeLayout) {
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                mAnchor.addView(holder.mTextureView, 0, lp);
            } else {
                throw new RuntimeException("unsupported anchor layout type");
            }
            mMediaPlayer.setOnMessageListener(this);
            if (mCallback != null) {
                mCallback.onPrePlay();
            }
            mMediaPlayer.playUrl((String) getTag());
//            mLoadingView.setVisibility(View.VISIBLE);
            mMediaPlayerCreated = true;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        show(3600000);
        mDragging = true;
        mUiHandler.removeMessages(SHOW_PROGRESS);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mDragging = false;

        long duration = mMediaPlayer.getLength();
        long newPosition = (duration * seekBar.getProgress()) / 1000L;
        mMediaPlayer.setTime(newPosition);
//        mPositionTv.setText(stringForTime((int) newPosition));

//        setProgress();
//        updatePausePlay();
        show();

        // Ensure that progress is properly updated in the future,
        // the call to show() does not guarantee this because it is a
        // no-op if we are already showing.
        mUiHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    @Override
    public void handleMsg(BasePlayer.MsgParams msgParams) {

    }

    @Override
    public void fullScreen(ViewGroup wrapper) {
        if (mAnchorListWrapper != mAnchor.getParent() || wrapper == mAnchorListWrapper) {
            throw new RuntimeException("anchor parent error");
        }
        mAnchorListWrapper.removeView(mAnchor);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        wrapper.addView(mAnchor, lp);
    }

    @Override
    public void quitFullScreen(ViewGroup wrapper) {
        if (mAnchor.getParent() != wrapper) {
            throw new RuntimeException("anchor parent error");
        }
        wrapper.removeView(mAnchor);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, mContext.getResources().getDisplayMetrics()));
        mAnchorListWrapper.addView(mAnchor, 0, lp);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams lp2 = mAnchor.getLayoutParams();
                LLog.d(TAG, "[" + mAnchor + ", " + lp2.width + ", " + lp2.height + "]");
                for (int i = 0; i < mAnchor.getChildCount(); i++) {
                    View child = mAnchor.getChildAt(i);
                    ViewGroup.LayoutParams lp1 = child.getLayoutParams();
                    LLog.d(TAG, "[" + child + ", " + lp1.width + ", " + lp1.height + "]");
                }
            }
        }, 2000);
    }

    public void setOnResumed(boolean onResumed) {
        mOnResumed = onResumed;
    }

}
