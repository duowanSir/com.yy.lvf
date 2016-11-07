package com.yy.lvf.player.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yy.lvf.R;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

import tv.danmaku.ijk.media.example.widget.media.IMediaController;
import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by slowergun on 2016/10/26.
 */
public class CommonMediaController extends FrameLayout implements IMediaController
        , View.OnClickListener
        , View.OnTouchListener
        , SeekBar.OnSeekBarChangeListener
        , IMediaPlayer.OnPreparedListener
        , IMediaPlayer.OnCompletionListener
        , IMediaPlayer.OnInfoListener
        , IMediaPlayer.OnErrorListener {
    public static class UiHandler extends Handler {
        private WeakReference<CommonMediaController> mController;

        public UiHandler(CommonMediaController controller) {
            mController = new WeakReference<CommonMediaController>(controller);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mController.get() == null) {
                return;
            }
            switch (msg.what) {
                case SHOW_PROGRESS:
                    int pos = mController.get().setProgress();
                    if (!mController.get().mDragging && mController.get().mShowing && mController.get().mMediaPlayerControlImpl.isPlaying()) {
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
                    mController.get().mUrlHadSet = false;
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
//        void onFullScreen(final View anchor);
//
//        void onQuitFullScreen(final View anchor);

        void onPrePlay();
    }

    public static final String TAG = CommonMediaController.class.getSimpleName();

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int UPDATE_PLAY_AND_PAUSE = 4;
    private static final int PREPARED = 8;
    private static final int ERROR = 16;
    private static final int COMPLETED = 32;
    private static final int BUFFERING_START = 64;
    private static final int BUFFERING_END = 128;
    private static final int VIDEO_RENDERING_START = 256;

    private Context mContext;
    private LayoutInflater mInflater;
    private MediaController.MediaPlayerControl mMediaPlayerControlImpl;
    private View mAnchor;
    private Callback mCallback;
    private UiHandler mUiHandler;
    /**
     * remove
     */
    private LayoutParams mLayout0Lp;
    private View mLayout0;
    private ImageView mPlayIv;
    private TextView mPositionTv;
    private SeekBar mProgressSb;
    private TextView mDurationTv;
    private ImageView mFullIv;

    private LayoutParams mReturnIvLp;
//    private ImageView mReturnIv;
    /**
     * remove
     */

    /**
     * gone
     */
    private ImageView mCentralPlayIv;
    private PercentageView mLoadingView;
    /**
     * gone
     */

    private boolean mShowing = false;
    private boolean mDragging = false;// 是否在拖拽的过程中
    private boolean mIsFullScreen = false;
    private boolean mUrlHadSet = false;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    public CommonMediaController(Context context) {
        this(context, null);
    }

    public CommonMediaController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonMediaController(Context context, AttributeSet attrs, int defStyleAttr) {
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
        initCentralPlayIv();
        initLoadingView();
        initLayout0();
//        initReturnIv();
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

    private boolean isInPlaybackState() {
        if (mMediaPlayerControlImpl != null) {
            return (((IjkVideoView) mMediaPlayerControlImpl).isInPlaybackState() || mMediaPlayerControlImpl.isPlaying());
        }
        return false;
    }

    public void pauseUpdate() {
        if (isInPlaybackState()) {
            mMediaPlayerControlImpl.pause();
            updatePausePlay();
        }
    }

    public void reset() {
        mShowing = false;
        mDragging = false;
        mUrlHadSet = false;

        mLoadingView.setVisibility(GONE);
        mCentralPlayIv.setVisibility(View.VISIBLE);
        mPlayIv.setImageResource(R.mipmap.media_controller_play_icon);
        mPositionTv.setText("");
        mProgressSb.setProgress(0);
        mDurationTv.setText("");

        removeView(mLayout0);
        mUiHandler.removeCallbacksAndMessages(null);
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

    @Override
    public void hide() {
        if (mShowing) {
            mUiHandler.removeMessages(SHOW_PROGRESS);
            // 官方使用mWindowManager.removeView(mDecor)实现隐藏功能
            removeView(mLayout0);
            mShowing = false;
        }
    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }

    @Override
    public void setAnchorView(View view) {
        mAnchor = view;
        if (getParent() != null) {
            ((FrameLayout) view).removeView(this);
        }
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        ((FrameLayout) view).addView(this, lp);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mLayout0.setEnabled(enabled);
    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mMediaPlayerControlImpl = player;
    }

    @Override
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

    @Override
    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    @Override
    public void showOnce(View view) {

    }

    private void updatePausePlay() {
        if (mMediaPlayerControlImpl.isPlaying()) {
            mPlayIv.setImageResource(R.mipmap.media_controller_pause_icon);
            mCentralPlayIv.setVisibility(GONE);
        } else {
            mPlayIv.setImageResource(R.mipmap.media_controller_play_icon);
            if (mLoadingView.getVisibility() == GONE) {
                mCentralPlayIv.setVisibility(VISIBLE);
            }
        }
    }

    public void doPauseResume() {
        if (mMediaPlayerControlImpl.isPlaying()) {
            mMediaPlayerControlImpl.pause();
            updatePausePlay();
        } else {
            mMediaPlayerControlImpl.start();
            mUiHandler.sendEmptyMessageDelayed(UPDATE_PLAY_AND_PAUSE, 500);
        }
    }

    private int setProgress() {
        if (mMediaPlayerControlImpl == null || mDragging) {
            return 0;
        }
        int position = mMediaPlayerControlImpl.getCurrentPosition();
        int duration = mMediaPlayerControlImpl.getDuration();
        if (mProgressSb != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mProgressSb.setProgress((int) pos);
            }
            int percent = mMediaPlayerControlImpl.getBufferPercentage();
            mProgressSb.setSecondaryProgress(percent * 10);
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
        if (v == this && event.getAction() == MotionEvent.ACTION_UP) {
            if (mShowing) {
                hide();
            } else {
                show(Integer.MAX_VALUE);
            }
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view == mPlayIv) {
            playBtnAction();
        } else if (view == mFullIv) {
        } else if (view == mCentralPlayIv) {
            mCentralPlayIv.setVisibility(GONE);
            playBtnAction();
        }
    }

    private void playBtnAction() {
        if (mCallback != null) {
            mCallback.onPrePlay();
        }
        if (!mUrlHadSet) {
            ((IjkVideoView) mMediaPlayerControlImpl).setVideoPath((String) mAnchor.getTag());
            mMediaPlayerControlImpl.start();
            mLoadingView.setVisibility(View.VISIBLE);
            mUrlHadSet = true;
        } else {
            doPauseResume();
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

        long duration = mMediaPlayerControlImpl.getDuration();
        long newPosition = (duration * seekBar.getProgress()) / 1000L;
        mMediaPlayerControlImpl.seekTo((int) newPosition);
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
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        mUiHandler.sendEmptyMessage(COMPLETED);
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        mUiHandler.sendEmptyMessage(ERROR);
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        Message msg = null;
        switch (i) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                msg = mUiHandler.obtainMessage(BUFFERING_START);
                msg.arg1 = i1;
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                msg = mUiHandler.obtainMessage(BUFFERING_END);
                msg.arg1 = i1;
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                msg = mUiHandler.obtainMessage(VIDEO_RENDERING_START);
                break;
            default:
                msg = null;
                break;
        }
        if (msg != null) {
            mUiHandler.sendMessage(msg);
        }
        return true;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mUiHandler.sendEmptyMessage(PREPARED);
    }
}
