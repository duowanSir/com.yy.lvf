package com.android.lvf.demo.component;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.android.lvf.R;

/**
 * Created by slowergun on 2016/12/13.
 */
public class ActivityBase extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private TextView mInfo;
    private CheckBox mNewTaskCb;
    private CheckBox mSingleTopCb;
    private CheckBox mClearTopCb;

    private boolean mFlagNewTask;
    private boolean mFlagSingleTop;
    private boolean mFlagClearTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        mInfo = (TextView) findViewById(R.id.info_tv);
        mNewTaskCb = (CheckBox) findViewById(R.id.new_task_cb);
        mSingleTopCb = (CheckBox) findViewById(R.id.single_top_cb);
        mClearTopCb = (CheckBox) findViewById(R.id.clear_top_cb);

        mSingleTopCb.setOnCheckedChangeListener(this);
        mNewTaskCb.setOnCheckedChangeListener(this);
        mClearTopCb.setOnCheckedChangeListener(this);
        mInfo.setText(getClass().getSimpleName());
        mInfo.setText(mInfo.getText() + "\n" + "onCreate(" + savedInstanceState + ")");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.standard) {
            Intent intent = new Intent(this, ActivityStandard.class);
            setFlag(intent);
            startActivity(intent);
        } else if (v.getId() == R.id.single_top) {
            Intent intent = new Intent(this, ActivitySingleTop.class);
            setFlag(intent);
            startActivity(intent);
        } else if (v.getId() == R.id.single_task) {
            Intent intent = new Intent(this, ActivitySingleTask.class);
            setFlag(intent);
            startActivity(intent);
        } else if (v.getId() == R.id.single_instance) {
            Intent intent = new Intent(this, ActivitySingleInstance.class);
            setFlag(intent);
            startActivity(intent);
        } else {
            throw new RuntimeException("click on unsupported view");
        }
    }

    private void setFlag(Intent intent) {
        if (mFlagSingleTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        if (mFlagNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (mFlagClearTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        mInfo.setText(mInfo.getText() + "\n" + "flag:[" + intent.getFlags() + "]");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mSingleTopCb) {
            mFlagSingleTop = isChecked;
        } else if (buttonView == mNewTaskCb) {
            mFlagNewTask = isChecked;
        } else if (buttonView == mClearTopCb) {
            mFlagClearTop = isChecked;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mInfo.setText(mInfo.getText() + "\n" + "onNewIntent(" + intent + ")");
    }
}
