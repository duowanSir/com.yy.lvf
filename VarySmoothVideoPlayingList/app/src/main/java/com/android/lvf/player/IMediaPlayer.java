package com.android.lvf.player;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.Map;

/**
 * Created by slowergun on 2016/11/23.
 */
public interface IMediaPlayer {
    void setDataSource(Context context, Uri uri);

    void setDataSource(Context context, Uri uri, Map<String, String> headers);

    void setSurface(Surface surface);

    void setSurfaceHolder(SurfaceHolder holder);

    void prepare();

    void prepareAsync();

    void start();

    void stop();

    void pause();

    int getVideoWidth();

    int getVideoHeight();

    boolean isPlaying();

    void seekTo(long msec);

    long getCurrentPosition();

    long getDuration();

    void release();

    void reset();

    void setAudioSessionId(int id);

    int getAudioSessionId();

    void setOnPreparedListener(OnPreparedListener listener);

    void setOnCompletionListener(OnCompletionListener listener);

    void setOnErrorListener(OnErrorListener listener);

    void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

    void setOnInfoListener(OnInfoListener listener);

    interface OnPreparedListener {
        void onPrepared(IMediaPlayer mp);
    }

    interface OnCompletionListener {
        void onCompletion(IMediaPlayer mp);
    }

    interface OnErrorListener {
        void onError(IMediaPlayer mp, int what, int extra);
    }

    interface OnBufferingUpdateListener {
        void onBufferingUpdate(IMediaPlayer mp, int percent);
    }

    interface OnInfoListener {
        void onInfo(IMediaPlayer mp, int what, int extra);
    }

}
