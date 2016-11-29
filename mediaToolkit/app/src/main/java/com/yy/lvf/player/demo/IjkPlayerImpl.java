package com.yy.lvf.player.demo;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.yy.lvf.player.IMediaPlayer;

import java.util.Map;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by slowergun on 2016/11/28.
 */
public class IjkPlayerImpl implements IMediaPlayer,
        tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener,
        tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener,
        tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener,
        tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener {
    private IjkMediaPlayer mIjkMediaPlayer;

    public IjkPlayerImpl() {
        mIjkMediaPlayer = new IjkMediaPlayer();
        mIjkMediaPlayer.setOnPreparedListener(this);
        mIjkMediaPlayer.setOnCompletionListener(this);
        mIjkMediaPlayer.setOnErrorListener(this);
        mIjkMediaPlayer.setOnInfoListener(this);
    }

    @Override
    public void setDataSource(Context context, Uri uri) {

    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) {

    }

    @Override
    public void setSurface(Surface surface) {

    }

    @Override
    public void setSurfaceHolder(SurfaceHolder holder) {

    }

    @Override
    public void prepare() {

    }

    @Override
    public void prepareAsync() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getVideoWidth() {
        return 0;
    }

    @Override
    public int getVideoHeight() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public void seekTo(long msec) {

    }

    @Override
    public long getCurrentPosition() {
        return 0;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public void release() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void setAudioSessionId(int id) {

    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {

    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {

    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {

    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {

    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {

    }

    @Override
    public void onCompletion(tv.danmaku.ijk.media.player.IMediaPlayer mp) {

    }

    @Override
    public boolean onError(tv.danmaku.ijk.media.player.IMediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(tv.danmaku.ijk.media.player.IMediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(tv.danmaku.ijk.media.player.IMediaPlayer mp) {

    }
}
