package com.android.lvf.demo.event;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lvf.R;

/**
 * Created by slowergun on 2016/12/8.
 */
public class ActivityVideoList extends Activity implements View.OnClickListener {
    public static class Holder {
        public int      mPosition;
        public TextView mTv;
    }

    public static final String TAG = ActivityVideoList.class.getSimpleName();

    private DebugFrameLayout  mDebugFrameLayout;
    private RelativeLayout    mCurtainLayout;
    private TextView          mInfoTv;
    private DebugLinearLayout mVideoContainerLayout;
    private View              mVideoView;
    private ListView          mLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        mDebugFrameLayout = (DebugFrameLayout) findViewById(R.id.debug_frame_layout);
        mCurtainLayout = (RelativeLayout) findViewById(R.id.curtain_layout);
        mInfoTv = (TextView) findViewById(R.id.info_tv);
        mVideoContainerLayout = (DebugLinearLayout) findViewById(R.id.video_container_layout);
        mVideoView = findViewById(R.id.video_view);
        mLv = (ListView) findViewById(R.id.lv);

        mCurtainLayout.setVisibility(View.GONE);
        mVideoContainerLayout.setOnClickListener(this);
        mVideoView.setOnClickListener(this);
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
                Holder holder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(ActivityVideoList.this).inflate(R.layout.base_list_item, parent, false);
                    holder = new Holder();
                    holder.mTv = (TextView) convertView.findViewById(R.id.tv);
                    convertView.setTag(holder);
                    convertView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Holder holder = (Holder) v.getTag();
                            if (mCurtainLayout.getVisibility() == View.GONE) {
                                Toast.makeText(ActivityVideoList.this, holder.mTv.getText(), Toast.LENGTH_SHORT).show();
                                Rect drawingRect = new Rect();
                                v.getDrawingRect(drawingRect);
                                Rect displayRect = new Rect();
                                v.getWindowVisibleDisplayFrame(displayRect);
                                mInfoTv.setText("[" + drawingRect.left + ", " + drawingRect.top + ", " + drawingRect.right + ", " + drawingRect.bottom + "]" + "\n" +
                                        "[" + displayRect.left + ", " + displayRect.top + ", " + displayRect.right + ", " + displayRect.bottom + "]" + "\n" +
                                        "[" + v.getLeft() + ", " + v.getTop() + ", " + v.getRight() + ", " + v.getBottom() + "]");
                                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mVideoContainerLayout.getLayoutParams();
                                lp.height = drawingRect.height();
                                lp.topMargin = v.getTop();
                                mVideoContainerLayout.setLayoutParams(lp);
                                mCurtainLayout.setVisibility(View.VISIBLE);
                                mDebugFrameLayout.setView(mCurtainLayout);
                                setListViewChildrenVisibility(View.INVISIBLE);
                            }
                        }
                    });
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.mPosition = position;
                holder.mTv.setText((String) getItem(position));
                holder.mTv.setTag(holder);
                return convertView;
            }
        });
        mLv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mCurtainLayout.setVisibility(View.GONE);
                mDebugFrameLayout.setView(null);
                setListViewChildrenVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.video_view) {
            Toast.makeText(this, "child", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.video_container_layout) {
            Toast.makeText(this, "parent", Toast.LENGTH_SHORT).show();
        }
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

    private void setListViewChildrenVisibility(int visibility) {
        int childrenCount = mLv.getChildCount();
        for (int i = 0; i < childrenCount; i++) {
            View child = mLv.getChildAt(i);
            child.setVisibility(visibility);
        }
    }
}
