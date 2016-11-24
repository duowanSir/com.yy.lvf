package com.yy.lvf.player;

import android.widget.MediaController;

/**
 * Created by slowergun on 2016/11/23.
 */
public interface IMediaPlayerControl {
    void start();

    void pause();

    int getDuration();

    int getCurrentPosition();

    void seekTo(int pos);

    boolean isPlaying();

    int getBufferPercentage();

    boolean canPause();

    boolean canSeekBackward();

    boolean canSeekForward();

    int getAudioSessionId();
}
