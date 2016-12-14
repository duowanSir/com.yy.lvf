package com.android.lvf.demo.event;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lvf.R;

/**
 * Created by slowergun on 2016/12/8.
 */
public class ActivityGestureDetect extends Activity implements CompoundButton.OnCheckedChangeListener {
    public static class Holder {
        public int      mPosition;
        public TextView mTv;
    }

    public static final String TAG = ActivityGestureDetect.class.getSimpleName();

    private FrameLayout mContainerLayout;
    private ListView    mLv;
    private DebugLayout mDebugLayout;

    private CheckBox mDownCb;
    private CheckBox mSingleTapUpCb;
    private CheckBox mScrollCb;
    private CheckBox mFlingCb;
    private CheckBox mDoubleTapCb;
    private CheckBox mDoubleTapEventCb;
    private CheckBox mSingleTapConfirmedCb;
    private CheckBox mContextClickCb;

    private boolean mFlagDown;
    private boolean mFlagSingleTapUp;
    private boolean mFlagScroll;
    private boolean mFlagFling;
    private boolean mFlagDoubleTap;
    private boolean mFlagDoubleTapEvent;
    private boolean mFlagSingleTapConfirmed;
    private boolean mFlagContextClick;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mDownCb) {
            mFlagDown = isChecked;
        } else if (buttonView == mSingleTapUpCb) {
            mFlagSingleTapUp = isChecked;
        } else if (buttonView == mScrollCb) {
            mFlagScroll = isChecked;
        } else if (buttonView == mFlingCb) {
            mFlagFling = isChecked;
        } else if (buttonView == mDoubleTapCb) {
            mFlagDoubleTap = isChecked;
        } else if (buttonView == mDoubleTapEventCb) {
            mFlagDoubleTapEvent = isChecked;
        } else if (buttonView == mSingleTapConfirmedCb) {
            mFlagSingleTapConfirmed = isChecked;
        } else if (buttonView == mContextClickCb) {
            mFlagContextClick = isChecked;
        } else {
            throw new RuntimeException("unsupported check event");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContainerLayout = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.activity_gesture_detect, null);
        setContentView(mContainerLayout);
        mLv = (ListView) findViewById(R.id.lv);
        mDebugLayout = (DebugLayout) findViewById(R.id.debug_layout);
        mDownCb = (CheckBox) findViewById(R.id.down_cb);
        mSingleTapUpCb = (CheckBox) findViewById(R.id.single_tap_up_cb);
        mScrollCb = (CheckBox) findViewById(R.id.scroll_cb);
        mFlingCb = (CheckBox) findViewById(R.id.fling_cb);
        mDoubleTapCb = (CheckBox) findViewById(R.id.double_tap_cb);
        mDoubleTapEventCb = (CheckBox) findViewById(R.id.double_tap_event_cb);
        mSingleTapConfirmedCb = (CheckBox) findViewById(R.id.single_tap_confirmed_cb);
        mContextClickCb = (CheckBox) findViewById(R.id.context_click_cb);

        mDownCb.setOnCheckedChangeListener(this);
        mSingleTapUpCb.setOnCheckedChangeListener(this);
        mScrollCb.setOnCheckedChangeListener(this);
        mFlingCb.setOnCheckedChangeListener(this);
        mDoubleTapCb.setOnCheckedChangeListener(this);
        mDoubleTapEventCb.setOnCheckedChangeListener(this);
        mSingleTapConfirmedCb.setOnCheckedChangeListener(this);
        mContextClickCb.setOnCheckedChangeListener(this);

        mLv.setAdapter(new BaseAdapter() {

            @Override
            public int getCount() {
                return 20;
            }

            @Override
            public Object getItem(int position) {
                return String.valueOf(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final Holder holder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(ActivityGestureDetect.this).inflate(R.layout.base_list_item, parent, false);
                    holder = new Holder();
                    holder.mTv = (TextView) convertView.findViewById(R.id.tv);
                    holder.mTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(ActivityGestureDetect.this, holder.mTv.getText(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.mPosition = position;
                holder.mTv.setText((String) getItem(position));
                holder.mTv.setTag(holder);
                return convertView;
            }
        });
<<<<<<< HEAD:VaryDemo/app/src/main/java/com/android/lvf/demo/event/ListCoverPlayActivity.java
        mDebugLayout.setGestureDetector(new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                LLog.d(TAG, "onDown(" + e.getAction() + ", " + mFlagDown + ")");
                return mFlagDown;
            }

            @Override
            public void onShowPress(MotionEvent e) {
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                LLog.d(TAG, "onSingleTapUp(" + e.getAction() + ", " + mFlagSingleTapUp + ")");
                return mFlagSingleTapUp;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                LLog.d(TAG, "onScroll(" + e1.getAction() + ", " + mFlagScroll + ")");
                mLv.setVisibility(View.VISIBLE);
                mLv.requestFocus();
                mLv.onTouchEvent(e1);
                return mFlagScroll;
            }

            @Override
            public void onLongPress(MotionEvent e) {
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                LLog.d(TAG, "onFling(" + e1.getAction() + ", " + mFlagFling + ")");
                return mFlagFling;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                LLog.d(TAG, "onDoubleTap(" + e.getAction() + ", " + mFlagDoubleTap + ")");
                return mFlagDoubleTap;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                LLog.d(TAG, "onDoubleTapEvent(" + e.getAction() + ", " + mFlagDoubleTapEvent + ")");
                return mFlagDoubleTapEvent;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                LLog.d(TAG, "onSingleTapConfirmed(" + e.getAction() + ", " + mFlagSingleTapConfirmed + ")");
                return mFlagSingleTapConfirmed;
            }

            @Override
            public boolean onContextClick(MotionEvent e) {
                LLog.d(TAG, "onContextClick(" + e.getAction() + ", " + mFlagContextClick + ")");
                return mFlagContextClick;
            }
        }));
=======
>>>>>>> 7c2d1203039d1ac23f551431b18d44e296aa1ee9:VaryDemo/app/src/main/java/com/android/lvf/demo/event/ActivityGestureDetect.java
    }

    private int mLvHeight = -1;


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mLvHeight == -1) {
            Rect rect = new Rect();
            mLv.getWindowVisibleDisplayFrame(rect);
            mLvHeight = rect.height();
        }
    }
}
