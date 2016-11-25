package com.yy.lvf.player.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.yy.lvf.LLog;
import com.yy.lvf.player.IMediaController;
import com.yy.lvf.player.IMediaPlayer;
import com.yy.lvf.player.IMediaPlayerControl;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by slowergun on 2016/11/23.
 */
public class TextureVideoView extends ScalableTextureView implements IMediaPlayerControl,
        TextureView.SurfaceTextureListener,
        IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnBufferingUpdateListener,
        IMediaPlayer.OnInfoListener {
    public static final  String   TAG                = TextureVideoView.class.getSimpleName();
    private static final int      TRY_LOCK_TIME_OUT  = 10;
    private static final TimeUnit TRY_LOCK_TIME_UNIT = TimeUnit.MILLISECONDS;
    // settable by the client
    private Uri                 mUri;
    private Map<String, String> mHeaders;

    // 所有可能的状态
    private static final int STATE_ERROR              = -1;
    private static final int STATE_IDLE               = 0;
    private static final int STATE_PREPARING          = 1;
    private static final int STATE_PREPARED           = 2;
    private static final int STATE_PLAYING            = 3;
    private static final int STATE_PAUSED             = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private int mCurrentState = STATE_IDLE;
    private int mTargetState  = STATE_IDLE;

    private Surface          mSurface;
    private IMediaController mMediaController;
    private IMediaPlayer     mMediaPlayer;
    private int              mAudioSession;

    private int     mCurrentBufferPercentage;
    private int     mSeekWhenPrepared;// preparing状态时,记下seek的位置.
    private boolean mCanPause;
    private boolean mCanSeekBack;
    private boolean mCanSeekForward;

    /*
    * listeners*/
    private IMediaPlayer.OnPreparedListener        mOnPreparedListener;
    private IMediaPlayer.OnCompletionListener      mOnCompletionListener;
    private IMediaPlayer.OnErrorListener           mOnErrorListener;
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private IMediaPlayer.OnInfoListener            mOnInfoListener;

    public TextureVideoView(Context context) {
        this(context, null);
    }

    public TextureVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextureVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTextureVideoView();
    }

    private void initTextureVideoView() {
        mContentWidth = 0;
        mContentHeight = 0;
        setSurfaceTextureListener(this);

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    public void setVideoPath(String path) {
        setVideoUri(Uri.parse(path));
    }

    public void setVideoUri(Uri uri) {
        setVideoUri(uri, null);
    }

    public void setVideoUri(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        LLog.d(TAG, "onSurfaceTextureAvailable(" + surface + ", " + width + ", " + height + ")");
        mSurface = new Surface(surface);
        openVideo();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        LLog.d(TAG, "onSurfaceTextureSizeChanged(" + surface + ", " + width + ", " + height + ")");
        LLog.d(TAG, getTextureVideoViewLog());
        if (mTargetState == STATE_PLAYING) {
            if (mSeekWhenPrepared != 0) {
                seekTo(mSeekWhenPrepared);
            }
            start();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurface = null;
        release(true);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(IMediaPlayer mp) {

    }

    @Override
    public void onError(IMediaPlayer mp, int what, int extra) {

    }

    @Override
    public void onInfo(IMediaPlayer mp, int what, int extra) {

    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        mCurrentState = STATE_PREPARED;

        /*
        * 获取播放器处理该视频流的能力*/
//        Get the capabilities of the player for this stream
//        Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
//                MediaPlayer.BYPASS_METADATA_FILTER);
//        if (data != null) {
//            mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
//                    || data.getBoolean(Metadata.PAUSE_AVAILABLE);
//            mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
//                    || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
//            mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
//                    || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
//        } else {
//            mCanPause = mCanSeekBack = mCanSeekForward = true;
//        }
        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(mp);
        }
        if (mMediaController != null) {
            mMediaController.setEnable(true);
        }
        if (mSeekWhenPrepared != 0) {
            seekTo(mSeekWhenPrepared);
            mSeekWhenPrepared = 0;
        }
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        if (videoWidth != 0 && videoHeight != 0) {
            setContentSize(videoWidth, videoHeight);
            if (mTargetState == STATE_PLAYING) {
                start();
                if (mMediaController != null) {
                    mMediaController.show();
                }
                /*
                * 这里有一些逻辑省略*/
            }
        }
    }

    private void openVideo() {
        if (mUri == null || mSurface == null) {
            return;
            LLog.d(TAG, getTextureVideoViewLog());
        }
        /*
        * 已经播放过视频
        * 重新设置url,并播放.
        * surface创建比较靠后
        * */
        release(false);

        AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        mMediaPlayer = null;
                    /*
                    * 字幕功能先忽略*/
        if (mAudioSession != 0) {
            mMediaPlayer.setAudioSessionId(mAudioSession);
        } else {
            mAudioSession = mMediaPlayer.getAudioSessionId();
        }
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mCurrentBufferPercentage = 0;
        mMediaPlayer.setDataSource(getContext(), mUri, mHeaders);
        mMediaPlayer.setSurface(mSurface);
        mMediaPlayer.prepareAsync();
        // 这里不设置mTargetState的任一状态
        mCurrentState = STATE_PREPARING;
        attachMediaController();
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    @Override
    public void release(boolean clearTargetState) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mSurface = null;
            mCurrentState = STATE_IDLE;
            if (clearTargetState) {
                mTargetState = STATE_IDLE;
            }
            AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(pos);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = pos;
        }
    }

    @Override
    public boolean isPlaying() {
        if (isInPlaybackState()) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        return mAudioSession;
    }

    private String getTextureVideoViewLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").
                append(mUri).append("\n").
                append(mSurface).append(", ").append(mMediaPlayer).append("\n").
                append("state:[").append(mCurrentState).append(", ").append(mTargetState).append("]").append("\n").
                append("size:[").append(mContentWidth).append(", ").append(mContentHeight).append(", ").append(mMeasureWidth).append(", ").append(mMeasureHeight).append("]").append("\n").
                append(mCurrentBufferPercentage).append(", ").append(mSeekWhenPrepared).append(", ").append(mCanPause).append(", ").append(mCanSeekBack).append(", ").append(mCanSeekForward).
                append("]");
        return sb.toString();
    }
}
