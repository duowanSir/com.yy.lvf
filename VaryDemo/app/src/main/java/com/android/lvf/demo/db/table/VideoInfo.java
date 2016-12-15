package com.android.lvf.demo.db.table;

import com.android.lvf.LLog;
import com.android.lvf.demo.db.IBaseTable;
import com.android.lvf.demo.db.dao.VideoInfoDao;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by slowergun on 2016/12/14.
 */
public class VideoInfo implements IBaseTable {
    private static final String TAG = VideoInfo.class.getSimpleName();
    /**
     * 表字段,请勿改动顺序
     */
    public  Long                          mOId;
    public  Integer                       mServerTimestampS;
    public  Integer                       mClientTimestampS;
    public  Integer                       mVisibleTimeS;
    public  Boolean                       mUploaded;
    /**
     * 表字段,请勿改动顺序
     */
    private LinkedHashMap<String, Object> mColumn2Value;

    public VideoInfo() {
        this(null, null, null, null, null);
    }

    public VideoInfo(Long oId, Integer serverTimestampS, Integer clientTimestampS, Integer visibleTimeS, Boolean uploaded) {
        mOId = oId;
        mServerTimestampS = serverTimestampS;
        mClientTimestampS = clientTimestampS;
        mVisibleTimeS = visibleTimeS;
        mUploaded = uploaded;
    }

    @Override
    public LinkedHashMap<String, Object> getColumn2Value() {
        if (mColumn2Value == null) {
            mColumn2Value = new LinkedHashMap<>();
        }
        if (!mColumn2Value.containsKey(VideoInfoDao.COLUMN_NAMES[0])) {
            if (mOId != null) {
                mColumn2Value.put(VideoInfoDao.COLUMN_NAMES[0], mOId);
            }
            LLog.w(TAG, "column value map warn");
        }
        if (!mColumn2Value.containsKey(VideoInfoDao.COLUMN_NAMES[1])) {
            if (mServerTimestampS != null) {
                mColumn2Value.put(VideoInfoDao.COLUMN_NAMES[1], mServerTimestampS);
            }
            LLog.w(TAG, "column value map warn");
        }
        if (!mColumn2Value.containsKey(VideoInfoDao.COLUMN_NAMES[2])) {
            if (mClientTimestampS != null) {
                mColumn2Value.put(VideoInfoDao.COLUMN_NAMES[2], mClientTimestampS);
            }
            LLog.w(TAG, "column value map warn");
        }
        if (!mColumn2Value.containsKey(VideoInfoDao.COLUMN_NAMES[3])) {
            if (mVisibleTimeS != null) {
                mColumn2Value.put(VideoInfoDao.COLUMN_NAMES[3], mVisibleTimeS);
            }
            LLog.w(TAG, "column value map warn");
        }
        if (!mColumn2Value.containsKey(VideoInfoDao.COLUMN_NAMES[4])) {
            if (mUploaded != null) {
                mColumn2Value.put(VideoInfoDao.COLUMN_NAMES[4], mUploaded);
            }
            LLog.w(TAG, "column value map warn");
        }
        return mColumn2Value;
    }

}
