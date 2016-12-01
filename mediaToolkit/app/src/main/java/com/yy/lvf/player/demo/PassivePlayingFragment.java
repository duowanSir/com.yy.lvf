package com.yy.lvf.player.demo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.yy.lvf.LLog;
import com.yy.lvf.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slowergun on 2016/11/3.
 */
public class PassivePlayingFragment extends Fragment {
    public static final String TAG     = PassivePlayingFragment.class.getSimpleName();
    private             String mVideo  = "http://v1.dwstatic.com/zbsq/bidraft/e35612b7a9c9ae64d7d67a5c32662260.mp4";
    private             String mVideo1 = "http://v1.dwstatic.com/zbsq/bidraft/8d74f6d983b11b6edc1d9f5d14d471a4.mp4";
    private ListView                      mLv;
    private YYPlayerPassivePlayingAdapter mAdapter;
    private List<String>                  mData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_source_fragment, container, false);
        mLv = (ListView) view.findViewById(R.id.video_source_lv);
        initData();
        mAdapter = new YYPlayerPassivePlayingAdapter(getActivity(), mData);
        mLv.setAdapter(mAdapter);
        mLv.setOnScrollListener(mAdapter);
        mLv.setRecyclerListener(mAdapter);
        return view;
    }

    private void initData() {
        mData = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            if ((i % 2) == 0) {
                mData.add(mVideo);
            } else {
                mData.add(mVideo1);
            }
        }
    }
}
