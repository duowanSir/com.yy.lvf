package com.android.lvf.demo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.lvf.LLog;
import com.android.lvf.demo.db.dao.VideoInfoDao;

/**
 * Created by 烽 on 2016/12/14.
 */

class LDatabaseHelper extends SQLiteOpenHelper {
    public static final int    VERSION = 2;
    public static final String NAME    = "com.duowan.orz.db";
    public static final String TAG     = LDatabaseHelper.class.getSimpleName();

    public LDatabaseHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LLog.d(TAG, "onCreate(" + db + ")");
        db.execSQL(VideoInfoDao.CREATE.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion <= 2) {
            db.execSQL(VideoInfoDao.UPDATE_1_2.toString());
        }
    }
}
