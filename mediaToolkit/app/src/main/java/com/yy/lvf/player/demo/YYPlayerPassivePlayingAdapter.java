package com.yy.lvf.player.demo;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ycloud.playersdk.BasePlayer;
import com.ycloud.playersdk.YYTexTurePlayer;
import com.yy.lvf.LLog;
import com.yy.lvf.R;
import com.yy.lvf.player.IVideoListAdapter;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by slowergun on 2016/11/28.
 */
public class YYPlayerPassivePlayingAdapter extends BaseAdapter implements AbsListView.OnScrollListener,
        AbsListView.RecyclerListener,
        IVideoListAdapter {
    public static class Holder {
        public int             mPosition;
        public View            mItemView;// 用于计算在整个列表中的位置
        public View            mWrapView;// 用于计算播放Surface在item中的位置
        public TextView        mInfoTv;
        public YYTexTurePlayer mYYTexturePlayer;
        public TextureView     mTextureView;
    }

    public static class MainHandler extends Handler {
        public static final int MSG_PLAY                  = 0;
        public static final int MSG_RELEASE               = 1;
        public static final int MSG_ITEM_POSITION_CHANGED = 2;

        private int mCurrent = -1;
        private int mNext    = -1;
        private YYPlayerMessageListener mPlayerListener;
        private Map<Integer, Float>     posMapPercentage;
        private Map<Integer, Holder>    posMapHolder;

        private WeakReference<YYPlayerPassivePlayingAdapter> mAdapter;

        public MainHandler(YYPlayerPassivePlayingAdapter adapter) {
            super(Looper.getMainLooper());
            mAdapter = new WeakReference<>(adapter);
            mPlayerListener = new YYPlayerMessageListener(this);
            posMapPercentage = new LinkedHashMap<>();
            posMapHolder = new HashMap<>();
        }

        @Override
        public void handleMessage(Message msg) {
            if (mAdapter.get() == null) {
                removeCallbacksAndMessages(null);
                return;
            }
            Holder holder = null;
            if (msg.obj instanceof Holder) {
                holder = (Holder) msg.obj;
            }
            LLog.d(TAG, "handleMessage(" + msg + ")");
            switch (msg.what) {
                case MSG_PLAY:
                    play(msg.arg1 == 1 ? true : false, holder);
                    break;
                case MSG_RELEASE:
                    release(holder);
                    break;
                case MSG_ITEM_POSITION_CHANGED:
                    itemPositionChanged();
                    break;
                default:
                    throw new RuntimeException("unsupported msg");
            }
        }

        public void msgPlay(boolean needCheckVisibility, Holder holder) {
            if (hasMessages(MSG_PLAY)) {
                LLog.d(TAG, "too much MSG_PLAY");
                removeMessages(MSG_PLAY);
            }
            Message msg = obtainMessage(MSG_PLAY);
            msg.obj = holder;
            msg.arg1 = needCheckVisibility ? 1 : 0;
            sendMessage(msg);
        }

        public void msgRelease(Holder holder) {
            Message msg = obtainMessage(MSG_RELEASE);
            msg.obj = holder;
            sendMessage(msg);
        }

        public void msgItemPositionChanged() {
            if (hasMessages(MSG_ITEM_POSITION_CHANGED)) {
                removeMessages(MSG_ITEM_POSITION_CHANGED);
                LLog.d(TAG, "too much MSG_ITEM_POSITION_CHANGED");
            }
            sendEmptyMessage(MSG_ITEM_POSITION_CHANGED);
        }

        private void release(Holder holder) {
            if (holder.mYYTexturePlayer != null) {
                holder.mYYTexturePlayer.setOnMessageListener(null);
                holder.mYYTexturePlayer.releasePlayer();
                holder.mYYTexturePlayer = null;
            }
            if (holder.mTextureView != null) {
                holder.mTextureView.setVisibility(View.GONE);
                if (holder.mWrapView != null) {
                    ((ViewGroup) holder.mWrapView).removeView(holder.mWrapView);
                } else {
                    ((ViewGroup) holder.mItemView).removeView(holder.mWrapView);
                }
                holder.mTextureView = null;
            }
        }

        private void play(boolean needCheckVisibility, Holder holder) {
            if (holder.mYYTexturePlayer != null) {
                throw new RuntimeException("player must be released first");
            }
            if (holder.mTextureView != null) {
                throw new RuntimeException("old texture view must be released first");
            }
            if (needCheckVisibility && calcVisiblePercent(holder.mItemView, holder.mWrapView, mAdapter.get().mLvHeight) < PERCENTAGE_CAN_PLAY) {
                return;
            }
            holder.mYYTexturePlayer = new YYTexTurePlayer(mAdapter.get().mContext, null);
            holder.mYYTexturePlayer.setOnMessageListener(mPlayerListener);
            holder.mTextureView = (TextureView) holder.mYYTexturePlayer.getView();
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(600, 600);
            lp.gravity = Gravity.CENTER;
            ((FrameLayout) holder.mItemView).addView(holder.mTextureView, lp);
            holder.mYYTexturePlayer.playUrl(mAdapter.get().mData.get(holder.mPosition));
            mCurrent = holder.mPosition;
        }

        private void itemPositionChanged() {
            AbsListView mLv = mAdapter.get().mLv;
            int lvHeight = mAdapter.get().mLvHeight;
            if (mLv == null) {
                return;
            }
            posMapPercentage.clear();
            posMapHolder.clear();

            int childrenCount = mLv.getChildCount();
            int first = -1;
            for (int i = 0; i < childrenCount; i++) {
                View item = mLv.getChildAt(i);
                Holder holder = (Holder) item.getTag();
                if (mAdapter.get().getItemViewType(holder.mPosition) != Type.VIDEO.ordinal()) {
                    continue;
                }
                float percentage = calcVisiblePercent(holder.mItemView, holder.mWrapView, lvHeight);
                BigDecimal bd = new BigDecimal(percentage);
                percentage = bd.setScale(2, RoundingMode.HALF_UP).floatValue();
                holder.mInfoTv.setText("" + percentage);
                if (first == -1 && percentage >= PERCENTAGE_CAN_PLAY) {
                    first = holder.mPosition;
                }
                posMapPercentage.put(holder.mPosition, percentage);
                posMapHolder.put(holder.mPosition, holder);
            }

            int current = -1;
            LLog.d(TAG, "mCurrent:" + mCurrent);
            if (mCurrent != -1) {
                current = mCurrent;
                if (posMapPercentage.containsKey(current)) {
                    if (posMapPercentage.get(current) < PERCENTAGE_CAN_PLAY) {
                        msgRelease(posMapHolder.get(current));
                        current = -1;

                        if (first != -1 && current == -1) {
                            current = first;
                            msgPlay(false, posMapHolder.get(current));
                        }
                    }
                } else {
                    if (first != -1) {
                        current = first;
                        msgPlay(false, posMapHolder.get(current));
                    }
                }
            } else {
                if (first != -1 && current == -1) {
                    current = first;
                    msgPlay(false, posMapHolder.get(current));
                }
            }

            Set<Integer> positionSet = posMapPercentage.keySet();
            Iterator<Integer> iterator = positionSet.iterator();
            boolean hasTargetFound = false;
            while (iterator.hasNext()) {
                int next = iterator.next();
                if (current == next) {
                    hasTargetFound = true;
                } else if (posMapPercentage.get(next) >= PERCENTAGE_CAN_PLAY) {
                    mNext = next;
                    if (hasTargetFound) {
                        break;
                    }
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

        public YYPlayerMessageListener getPlayerListener() {
            return mPlayerListener;
        }

        public void pause() {
            if (mCurrent == -1) {
                return;
            }
            if (posMapHolder != null && posMapHolder.containsKey(mCurrent)) {
                Holder holder = posMapHolder.get(mCurrent);
                holder.mYYTexturePlayer.pausePlay();
            }
        }

        public void start() {
            if (mCurrent == -1) {
                return;
            }
            if (posMapHolder != null && posMapHolder.containsKey(mCurrent)) {
                Holder holder = posMapHolder.get(mCurrent);
                holder.mYYTexturePlayer.play();
            }
        }
    }

    public static class YYPlayerMessageListener implements BasePlayer.OnMessageListener {
        private WeakReference<MainHandler> mMainHandler;
        private boolean mOnResumed = true;

        public YYPlayerMessageListener(MainHandler handler) {
            mMainHandler = new WeakReference<MainHandler>(handler);
        }

        @Override
        public void handleMsg(BasePlayer.MsgParams msg) {
            if (mMainHandler.get() == null) {
                return;
            }
            LLog.d(TAG, "handleMsg(" + logMsg(msg) + ")");
            switch (msg.type) {
                case BasePlayer.MSG_PLAY_ERROR:
                case BasePlayer.MSG_PLAY_HARDDECODERERROR:
                    break;
                case BasePlayer.MSG_PLAY_END:
                    completion();
                    break;
                case BasePlayer.MSG_PLAYING:
//                    if (!mOnResumed) {
//                        mMainHandler.get().pause();
//                    }
                    break;
                default:
                    break;
            }
        }

        private void error() {

        }

        private void completion() {
            LLog.d(TAG, "completion(currentNext:" + mMainHandler.get().mNext + ")");
            if (mMainHandler.get().mNext == -1) {
                return;
            }
            mMainHandler.get().msgRelease(mMainHandler.get().posMapHolder.get(mMainHandler.get().mCurrent));
            mMainHandler.get().msgPlay(false, mMainHandler.get().posMapHolder.get(mMainHandler.get().mNext));
            Set<Integer> positionSet = mMainHandler.get().posMapPercentage.keySet();
            Iterator<Integer> iterator = positionSet.iterator();
            boolean found = false;
            int next = -1;
            while (iterator.hasNext()) {
                int item = iterator.next();
                if (item == mMainHandler.get().mNext) {
                    found = true;
                } else if (found && mMainHandler.get().posMapPercentage.get(item) >= PERCENTAGE_CAN_PLAY) {
                    next = item;
                }
            }
            mMainHandler.get().mNext = next;
            LLog.d(TAG, "completion(nextNext:" + mMainHandler.get().mNext + ")");
        }

        private String logMsg(BasePlayer.MsgParams msg) {
            if (msg == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("YYPlayerMsg:[").
                    append(msg.type).append(", ").
                    append(msg.param1).append(", ").
                    append(msg.param2).append(", ").
                    append(msg.param3).append(", ").
                    append(msg.bundle).
                    append("]");
            return new String(sb);
        }

        public void setOnResumed(boolean onResumed) {
            mOnResumed = onResumed;
        }
    }

    public enum Type {
        VIDEO
    }

    public static final String TAG                 = YYPlayerPassivePlayingAdapter.class.getSimpleName();
    public static final float  PERCENTAGE_CAN_PLAY = 0.8f;

    private Context        mContext;
    private LayoutInflater mInflater;
    private MainHandler    mMainHandler;

    private List<String> mData;

    public YYPlayerPassivePlayingAdapter(Context context, List<String> data) {
        super();
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(mContext);
        mMainHandler = new MainHandler(this);
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
        return Type.VIDEO.ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return Type.values().length;
    }

    private boolean mFirstVideo = true;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (getItemViewType(position) == Type.VIDEO.ordinal()) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.yyplayer_passive_playing_list_item, null);
                holder = new Holder();
                holder.mItemView = convertView;
                holder.mInfoTv = (TextView) convertView.findViewById(R.id.info_tv);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.mPosition = position;
            holder.mInfoTv.setText("");
            convertView.setTag(holder);

            if (mFirstVideo) {
                mMainHandler.msgPlay(true, holder);
                mMainHandler.msgItemPositionChanged();
                mFirstVideo = false;
            }

            return convertView;
        } else {
            throw new RuntimeException("unsupported item view type");
        }
    }

    private AbsListView mLv;
    private int         mScrollState;
    private int         mLvHeight;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        String state = null;
        if (scrollState == SCROLL_STATE_FLING) {
            state = "SCROLL_STATE_FLING";
        } else if (scrollState == SCROLL_STATE_IDLE) {// 终点
            state = "SCROLL_STATE_IDLE";
            LLog.d(TAG, "onScrollStateChanged(" + state + ")");
            mMainHandler.msgItemPositionChanged();
        } else if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {// 起点
            state = "SCROLL_STATE_TOUCH_SCROLL";
        }
        LLog.d(TAG, "onScrollStateChanged(" + state + ")");
        mScrollState = scrollState;
        if (mLv == null) {
            mLv = view;
            mLv.getWindowVisibleDisplayFrame(mItemRect);
            mLvHeight = mItemRect.height();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mLv == null) {
            mLv = view;
            mLv.getWindowVisibleDisplayFrame(mItemRect);
            mLvHeight = mItemRect.height();
        }
        if (mScrollState == SCROLL_STATE_TOUCH_SCROLL) {
            LLog.d(TAG, "onScroll(" + firstVisibleItem + ", " + visibleItemCount + ", " + totalItemCount + ", " + mScrollState + ")");
            mMainHandler.msgItemPositionChanged();
        }
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        Holder holder = (Holder) view.getTag();
        if (getItemViewType(holder.mPosition) == Type.VIDEO.ordinal()) {
            mMainHandler.msgRelease(holder);
        }
    }

    private Rect mItemRect = new Rect();

    @Override
    public void onPause() {
        if (mLv == null) {
            return;
        }
        mMainHandler.getPlayerListener().setOnResumed(false);
        mMainHandler.pause();
    }

    @Override
    public void onResume() {
        if (mLv == null) {
            return;
        }
        mMainHandler.getPlayerListener().setOnResumed(true);
        mMainHandler.start();
    }
}

