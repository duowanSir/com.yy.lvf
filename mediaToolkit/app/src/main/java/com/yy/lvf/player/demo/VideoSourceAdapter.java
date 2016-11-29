package com.yy.lvf.player.demo;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.yy.lvf.R;
import com.yy.lvf.player.view.CommonMediaController;

import java.util.List;

import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;

/**
 * Created by slowergun on 2016/11/3.
 */
public class VideoSourceAdapter extends BaseAdapter implements AbsListView.OnScrollListener,
        AbsListView.RecyclerListener {
    public static class Holder {
        public int mPosition;
        public IjkVideoView mVideoView;

        public CommonMediaController mMediaController;

    }

    public enum Type {
        VIDEO
    }

    public static final String TAG = VideoSourceAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mInflater;
    private DisplayMetrics mDm;
    private int mVideoVHeight;

    private List<String> mData;

    public VideoSourceAdapter(Context context, List<String> data) {
        super();
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(mContext);
        mDm = mContext.getResources().getDisplayMetrics();
        mVideoVHeight = (int) ((float) mDm.widthPixels / 16 * 9);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return Type.values().length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.video_source_item, null);
            holder = new Holder();
            holder.mVideoView = (IjkVideoView) convertView.findViewById(R.id.video_view);
            holder.mMediaController = new CommonMediaController(mContext);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mDm.widthPixels, mVideoVHeight);
            holder.mVideoView.setLayoutParams(lp);
            holder.mVideoView.setMediaController(holder.mMediaController);
            holder.mVideoView.setOnErrorListener(holder.mMediaController);
            holder.mVideoView.setOnPreparedListener(holder.mMediaController);
            holder.mVideoView.setOnInfoListener(holder.mMediaController);
            holder.mVideoView.setOnCompletionListener(holder.mMediaController);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.mPosition = position;
        holder.mVideoView.setTag(mData.get(position));
        convertView.setTag(holder);
        return convertView;
    }

    private AbsListView mLv;
    private int mScrollState;
    private int mFirstVisibleItem;
    private int mVisibleItemCount;
    private int mTotalItemCount;
    private int mLvHeight;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        String state = null;
        if (scrollState == SCROLL_STATE_FLING) {
            state = "SCROLL_STATE_FLING";
        } else if (scrollState == SCROLL_STATE_IDLE) {// 终点
            state = "SCROLL_STATE_IDLE";
            doPauseOnScroll();
        } else if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {// 起点
            state = "SCROLL_STATE_TOUCH_SCROLL";
        }
        Log.d(TAG, "onScrollStateChanged(" + state + ")");
        mScrollState = scrollState;
        if (mLv == null) {
            mLv = view;

            mLv.getWindowVisibleDisplayFrame(mItemRect);
            Log.d(TAG, "Rect(" + mItemRect.left + ", " + mItemRect.top + ", " + mItemRect.right + ", " + mItemRect.bottom + ")");
            mLv.getDrawingRect(mItemRect);
            Log.d(TAG, "Rect(" + mItemRect.left + ", " + mItemRect.top + ", " + mItemRect.right + ", " + mItemRect.bottom + ")");
            mLvHeight = mItemRect.height();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.d(TAG, "onScroll(" + firstVisibleItem + ", " + visibleItemCount + ", " + totalItemCount + ")");
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        mTotalItemCount = totalItemCount;

        if (mScrollState == SCROLL_STATE_TOUCH_SCROLL) {
            doPauseOnScroll();
        }
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        Log.d(TAG, "" + view);
        Holder holder = (Holder) view.getTag();
        if (getItemViewType(holder.mPosition) == Type.VIDEO.ordinal()) {
            holder.mMediaController.pauseUpdate();
        }
    }

    private Rect mItemRect = new Rect();

    private void doPauseOnScroll() {
        if (mLv == null) {
            return;
        }
        int childrenCount = mLv.getChildCount();
        for (int i = 0; i < childrenCount; i++) {
            View item = mLv.getChildAt(i);
            Holder holder = (Holder) item.getTag();
            if (getItemViewType(holder.mPosition) != Type.VIDEO.ordinal()) {
                continue;
            }
            boolean isVisibleItem = false;
            int lastVisibleItem = mFirstVisibleItem + mVisibleItemCount;
            for (int j = mFirstVisibleItem; j < lastVisibleItem; j++) {
                if (j >= mData.size()) {
                    continue;
                }
                String videoUrl = mData.get(j);
                if (!TextUtils.isEmpty(videoUrl) && videoUrl.equals(mData.get(holder.mPosition))) {
                    isVisibleItem = true;
                    break;
                }
            }
            if (isVisibleItem) {
                int top = item.getTop();
                int bottom = item.getBottom();
                Log.d(TAG, "[" + top + ", " + bottom + "]");
                int halfHeight = (bottom - top) >> 1;
                if ((top < 0 && top < -halfHeight) || (mLvHeight != 0 && bottom - mLvHeight > halfHeight)) {
                    holder.mMediaController.pauseUpdate();
                }
            }
        }
    }
}
