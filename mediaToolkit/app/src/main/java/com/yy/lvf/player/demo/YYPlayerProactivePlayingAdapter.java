package com.yy.lvf.player.demo;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ycloud.playersdk.YYTexTurePlayer;
import com.yy.lvf.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Created by slowergun on 2016/11/3.
 */
public class YYPlayerProactivePlayingAdapter extends BaseAdapter implements AbsListView.OnScrollListener,
        AbsListView.RecyclerListener,
        YYMediaController.Callback {
    public static class Holder {
        public int               mPosition;
        public View              mItemView;// 用于计算在整个列表中的位置
        public ViewGroup         mWrapView;// 用于计算播放Surface在item中的位置
        public TextView          mInfoTv;
        public YYTexTurePlayer   mYYTexturePlayer;
        public TextureView       mTextureView;
        public YYMediaController mYYMediaController;
    }

    public enum Type {
        VIDEO
    }

    public static final String TAG                 = YYPlayerProactivePlayingAdapter.class.getSimpleName();
    public static final float  PERCENTAGE_CAN_PLAY = 0.8f;

    private Context        mContext;
    private LayoutInflater mInflater;
    private DisplayMetrics mDm;
    private int            mVideoVHeight;

    private List<String> mData;

    public YYPlayerProactivePlayingAdapter(Context context, List<String> data) {
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
            convertView = mInflater.inflate(R.layout.yyplayer_proactive_playing_list_item, null);
            holder = new Holder();
            holder.mInfoTv = (TextView) convertView.findViewById(R.id.info_tv);
            holder.mItemView = convertView;
            holder.mWrapView = (ViewGroup) convertView.findViewById(R.id.texture_wrap_layout);
            holder.mYYMediaController = new YYMediaController(mContext);
            holder.mYYMediaController.setCallback(this);
        } else {
            holder = (Holder) convertView.getTag();
            holder.mYYMediaController.reset();
        }
        holder.mPosition = position;
        holder.mYYMediaController.setAnchorView(holder.mWrapView);
        holder.mYYMediaController.setTag(mData.get(position));
        convertView.setTag(holder);
        holder.mWrapView.setTag(holder);
        return convertView;
    }

    private AbsListView mLv;
    private int         mScrollState;
    private int         mFirstVisibleItem;
    private int         mVisibleItemCount;
    private int         mTotalItemCount;
    private int         mLvHeight;

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
            release(holder);
        }
    }

    private void release(Holder holder) {
        if (holder.mYYTexturePlayer != null) {
            holder.mYYTexturePlayer.setOnMessageListener(null);
            holder.mYYTexturePlayer.releasePlayer();
            holder.mYYTexturePlayer = null;
            holder.mYYMediaController.setMediaPlayer(null);
        }
        if (holder.mTextureView != null) {
            holder.mTextureView.setVisibility(View.GONE);
            if (holder.mWrapView != null) {
                ((ViewGroup) holder.mWrapView).removeView(holder.mTextureView);
            } else {
                ((ViewGroup) holder.mItemView).removeView(holder.mTextureView);
            }
            holder.mTextureView = null;
        }
        holder.mYYMediaController.reset();
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
            float percentage = calcVisiblePercent(holder.mItemView, holder.mWrapView, mLvHeight);
            BigDecimal bd = new BigDecimal(percentage);
            percentage = bd.setScale(2, RoundingMode.HALF_UP).floatValue();
            holder.mInfoTv.setText("" + percentage);
            if (holder.mYYTexturePlayer != null && holder.mTextureView != null && percentage < PERCENTAGE_CAN_PLAY) {
                release(holder);
            }
        }

    }

    public float calcVisiblePercent(View itemView, View wrapView, int boundaryOfHeight) {
        if (itemView == null) {
            throw new RuntimeException("list item view can not be null");
        }
        int top = 0, bottom = 0;
        if (wrapView != null) {
            top = wrapView.getTop();
            bottom = wrapView.getBottom();
            top += itemView.getTop();
            bottom += itemView.getTop();
        } else {
            top = itemView.getTop();
            bottom = itemView.getBottom();
        }
        int height = bottom - top;
        if (top < 0) {
            if (bottom < 0) {
                return 0;
            } else if (bottom >= 0 && bottom < boundaryOfHeight) {
                return (float) bottom / height;
            } else {
                return (float) boundaryOfHeight / height;
            }
        } else if (top >= 0 && top < boundaryOfHeight) {
            if (bottom < boundaryOfHeight) {
                return 1;
            } else {
                return (float) (boundaryOfHeight - top) / height;
            }
        } else {
            return 0;
        }
    }

    @Override
    public void onPrePlay() {

    }
}
