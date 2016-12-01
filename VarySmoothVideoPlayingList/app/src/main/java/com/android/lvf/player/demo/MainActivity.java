package com.android.lvf.player.demo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.android.lvf.R;
import com.android.lvf.player.IFullScreen;
import com.yy.lvf.LLog;


/**
 * Created by slowergun on 2016/10/28.
 */
public class MainActivity extends FragmentActivity implements IFullScreen.IFullScreenOwner {
    private static final String TAG                            = MainActivity.class.getSimpleName();
    private static final String TAG_PROACTIVE_PLAYING_FRAGMENT = "proactive_playing_fragment";
    private static final String TAG_PASSIVE_PLAYING_FRAGMENT   = "passive_playing_fragment";

    private FrameLayout mFragmentFl;
    private FrameLayout mVideoFl;
    private IFullScreen mFullScreenCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mFragmentFl = (FrameLayout) findViewById(R.id.fragment_fl);
        mVideoFl = (FrameLayout) findViewById(R.id.video_fl);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_fl, new ProactivePlayingFragment(), "proactive_playing_fragment").commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LLog.d(TAG, "onConfigurationChanged()");
        if (mFullScreenCallback != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mFullScreenCallback.fullScreen(mVideoFl);
                mFragmentFl.setVisibility(View.GONE);
                mVideoFl.setVisibility(View.VISIBLE);
            } else {
                mFullScreenCallback.quitFullScreen(mVideoFl);
                mFragmentFl.setVisibility(View.VISIBLE);
                mVideoFl.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.list_passive:
                Fragment passiveFragment = getSupportFragmentManager().findFragmentByTag(TAG_PASSIVE_PLAYING_FRAGMENT);
                if (passiveFragment == null) {
                    passiveFragment = new PassivePlayingFragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_fl, passiveFragment, TAG_PASSIVE_PLAYING_FRAGMENT).commit();
                break;
            case R.id.list_proactive:
                Fragment proactiveFragment = getSupportFragmentManager().findFragmentByTag(TAG_PROACTIVE_PLAYING_FRAGMENT);
                if (proactiveFragment == null) {
                    proactiveFragment = new ProactivePlayingFragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_fl, proactiveFragment, TAG_PROACTIVE_PLAYING_FRAGMENT).commit();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setFullScreenCallback(IFullScreen callback) {
        mFullScreenCallback = callback;
    }
}
