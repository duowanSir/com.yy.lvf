package com.android.lvf.demo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.android.lvf.demo.db.dao.VideoInfoDao;


/**
 * Created by 烽 on 2016/12/14.
 */

public class DaoManager {
    private static class HOLDER {
        private static DaoManager INSTANCE = new DaoManager();
    }

    //    public static final int      TIME = 10;
//    public static final TimeUnit UNIT = TimeUnit.MICROSECONDS;
    private Context         mContext;
    private LDatabaseHelper mOpenHelper;

    private SQLiteDatabase mSqLiteDatabase;

    private VideoInfoDao mVideoInfoDao;
//    private final ReentrantReadWriteLock mLock = new ReentrantReadWriteLock();

    private DaoManager() {
    }

    public synchronized void init(Context context) {
        if (mContext != null) {
            throw new RuntimeException("dao manager has been init");
        }
        mContext = context;
        mOpenHelper = new LDatabaseHelper(mContext, LDatabaseHelper.NAME, LDatabaseHelper.VERSION);

        mVideoInfoDao = new VideoInfoDao();
    }

    private void assertContext() {
        if (mContext == null) {
            throw new RuntimeException("dao manager must init first");
        }
    }

    public synchronized VideoInfoDao getVideoInfoDao() {
        assertContext();
        return mVideoInfoDao;
    }

    public synchronized SQLiteDatabase getWritableDatabase() {
        if (mSqLiteDatabase == null) {
            mSqLiteDatabase = mOpenHelper.getWritableDatabase();
        }
        return mSqLiteDatabase;
    }

    public static DaoManager getInstance() {
        return HOLDER.INSTANCE;
    }

}
