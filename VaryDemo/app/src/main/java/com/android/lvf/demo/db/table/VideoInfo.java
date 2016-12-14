package com.android.lvf.demo.db.table;

import com.android.lvf.demo.db.IBaseTable;

import java.util.Map;

/**
 * Created by slowergun on 2016/12/14.
 */
public class VideoInfo implements IBaseTable<VideoInfo> {
    /**
     * 表字段,请勿改动顺序
     */
    public  Long                mOId;
    public  Integer             mServerTimestampS;
    public  Integer             mClientTimestampS;
    public  Integer             mVisibleTimeS;
    public  Boolean             mUploaded;
    /**
     * 表字段,请勿改动顺序
     */
    private Map<String, Object> mColumn2Value;// 插入时有用
    private String[]            mValues;
    private Class<?>[]          mValueTypes;

    public VideoInfo() {
        this(null, null, null, null, null);
    }

    public VideoInfo(Long oId, Integer serverTimestampS, Integer clientTimestampS, Integer visibleTimeS, Boolean uploaded) {
        mOId = oId;
        mServerTimestampS = serverTimestampS;
        mClientTimestampS = clientTimestampS;
        mVisibleTimeS = visibleTimeS;
        mUploaded = uploaded;

        generateColumnValues();
        generateValueTypes();
    }

    @Override
    public String[] getValues() {
        return mValues;
    }

    @Override
    public Class<?>[] getValueTypes() {
        return mValueTypes;
    }

    @Override
    public VideoInfo newOne() {
        return new VideoInfo();
    }

    private void generateColumnValues() {
        mValues = new String[5];
        if (mOId == null) {
            mValues[0] = null;
        } else {
            mValues[0] = String.valueOf(mOId);
        }
        if (mServerTimestampS == null) {
            mValues[1] = null;
        } else {
            mValues[0] = String.valueOf(mServerTimestampS);
        }
        if (mClientTimestampS == null) {
            mValues[2] = null;
        } else {
            mValues[2] = String.valueOf(mClientTimestampS);
        }
        if (mVisibleTimeS == null) {
            mValues[3] = null;
        } else {
            mValues[3] = String.valueOf(mVisibleTimeS);
        }
        if (mUploaded == null) {
            mValues[4] = null;
        } else {
            mValues[4] = String.valueOf(mUploaded);
        }
    }

    private void generateValueTypes() {
        mValueTypes = new Class[5];
        mValueTypes[0] = Long.TYPE;
        mValueTypes[1] = Integer.TYPE;
        mValueTypes[2] = Integer.TYPE;
        mValueTypes[3] = Integer.TYPE;
        mValueTypes[4] = Boolean.TYPE;
    }

}
