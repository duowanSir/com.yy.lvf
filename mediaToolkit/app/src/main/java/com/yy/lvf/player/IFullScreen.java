package com.yy.lvf.player;

import android.view.ViewGroup;

/**
 * Created by slowergun on 2016/12/1.
 */
public interface IFullScreen {
    void fullScreen(final ViewGroup wrapper);

    void quitFullScreen(final ViewGroup wrapper);

    interface IFullScreenOwner {
        void setFullScreenCallback(IFullScreen callback);
    }
}
