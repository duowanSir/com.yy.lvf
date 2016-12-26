package com.android.lvf.demo.db.table;


import com.android.lvf.demo.db.IBaseTable;

import java.util.LinkedHashMap;

/**
 * Created by slowergun on 2016/12/14.
 */
public class VideoInfo implements IBaseTable {
    private static final String TAG = VideoInfo.class.getSimpleName();
    /**
     * 表字段,请勿改动顺序
     */
    public  Long                             mOId;
    public  Integer                          mServerTimestampS;
    public  Integer                          mClientTimestampS;
    public  Integer                          mVisibleTimeS;
    public  Boolean                          mUploaded;
    /**
     * 表字段,请勿改动顺序
     */
    private LinkedHashMap<Integer, Object>   mColumnIndex2Value;
    private LinkedHashMap<Integer, Class<?>> mColumnIndex2Type;

    public VideoInfo() {
        this(null, null, null, null, null);
    }

    public VideoInfo(Long oid) {
        this(oid, null, null, null, null);
    }

    public VideoInfo(Long oId, Integer serverTimestampS, Integer clientTimestampS, Integer visibleTimeS, Boolean uploaded) {
        mOId = oId;
        mServerTimestampS = serverTimestampS;
        mClientTimestampS = clientTimestampS;
        mVisibleTimeS = visibleTimeS;
        mUploaded = uploaded;
    }

    @Override
    public LinkedHashMap<Integer, Object> getColumnIndex2Value() {
        if (mColumnIndex2Value == null) {
            mColumnIndex2Value = new LinkedHashMap<>();
        }
        if (mOId != null) {
            mColumnIndex2Value.put(0, mOId);
        }
        if (mServerTimestampS != null) {
            mColumnIndex2Value.put(1, mServerTimestampS);
        }
        if (mClientTimestampS != null) {
            mColumnIndex2Value.put(2, mClientTimestampS);
        }
        if (mVisibleTimeS != null) {
            mColumnIndex2Value.put(3, mVisibleTimeS);
        }
        if (mUploaded != null) {
            mColumnIndex2Value.put(4, mUploaded);
        }
        return mColumnIndex2Value;
    }

    @Override
    public LinkedHashMap<Integer, Class<?>> getColumnIndex2Type() {
        if (mColumnIndex2Type == null) {
            mColumnIndex2Type = new LinkedHashMap<>();
        }
        mColumnIndex2Type.put(0, Long.class);
        mColumnIndex2Type.put(1, Integer.class);
        mColumnIndex2Type.put(2, Integer.class);
        mColumnIndex2Type.put(3, Integer.class);
        mColumnIndex2Type.put(4, Boolean.class);
        return mColumnIndex2Type;
    }

    @Override
    public void putByColumnIndex2Value(int index, Object value) {
        if (index == 0) {
            mOId = (Long) value;
        }
        if (index == 1) {
            mServerTimestampS = (Integer) value;
        }
        if (index == 2) {
            mClientTimestampS = (Integer) value;
        }
        if (index == 3) {
            mVisibleTimeS = (Integer) value;
        }
        if (index == 4) {
            mUploaded = (Boolean) value;
        }
    }

    @Override
    public VideoInfo create() {
        return new VideoInfo();
    }
}
