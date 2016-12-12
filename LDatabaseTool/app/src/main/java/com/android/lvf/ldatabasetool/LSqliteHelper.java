package com.android.lvf.ldatabasetool;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 烽 on 2016/12/1.
 */

public class LSqliteHelper extends SQLiteOpenHelper {
    private int mVersion = 0;
    private String mName;

    public LSqliteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    public LSqliteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        mVersion = version;
        mName = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
