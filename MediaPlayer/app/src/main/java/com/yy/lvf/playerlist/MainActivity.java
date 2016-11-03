package com.yy.lvf.playerlist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

/**
 * Created by slowergun on 2016/10/28.
 */
public class MainActivity extends FragmentActivity {
    private boolean mFlag = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        VideoSourceFragment fragment = new VideoSourceFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_fl, fragment, "video_source_fragment").commit();
    }
}
