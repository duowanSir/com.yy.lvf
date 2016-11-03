package com.yy.lvf.playerlist;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by slowergun on 2016/10/28.
 */
public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private boolean mFlag = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("MainActivity", "onCreate(" + savedInstanceState + ")");
        setContentView(R.layout.main_layout);

        findViewById(R.id.btn).setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v("MainActivity", "onSaveInstanceState(" + outState + ")");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.v("MainActivity", "onConfigurationChanged(" + newConfig + ")");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn) {
            if (!mFlag) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            mFlag = !mFlag;
        }
    }
}
