package com.yy.lvf.playerlist;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.List;

import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;

/**
 * Created by slowergun on 2016/11/3.
 */
public class VideoSourceAdapter extends BaseAdapter implements View.OnClickListener {
    public static final String TAG = VideoSourceAdapter.class.getSimpleName();

    public class Holder {
        public IjkVideoView mVideoV;
        public Button mPlayBtn;

        public int mPosition;
    }

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
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.video_source_item, null);
            holder = new Holder();
            holder.mVideoV = (IjkVideoView) convertView.findViewById(R.id.video_view);
            holder.mPlayBtn = (Button) convertView.findViewById(R.id.play_btn);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mDm.widthPixels, mVideoVHeight);
            holder.mVideoV.setLayoutParams(lp);
            holder.mPlayBtn.setOnClickListener(this);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.mPosition = position;
        convertView.setTag(holder);
        holder.mPlayBtn.setTag(holder);
        SpannableString ss = new SpannableString("ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
        ss.setSpan(new ForegroundColorSpan(0xff13579a), 0, ss.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        holder.mPlayBtn.setText(ss);
        holder.mPlayBtn.setMaxLines(2);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.play_btn) {
            Holder holder = (Holder) v.getTag();
            String video = mData.get(holder.mPosition);
            holder.mVideoV.setVideoPath(video);
            holder.mVideoV.start();
        }
    }
}
