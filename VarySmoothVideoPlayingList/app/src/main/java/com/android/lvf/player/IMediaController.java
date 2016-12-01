package com.android.lvf.player;

import android.view.View;

/**
 * Created by slowergun on 2016/11/25.
 */
public interface IMediaController {
    void show(int misc);

    void show();

    void hide();

    void setAnchor(View anchor);

    void setEnable(boolean enable);
}
