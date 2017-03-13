package com.android.lvf.demo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


/**
 * Created by 烽 on 2016/12/14.
 */

public class DatabaseManager {
    private Context         context;
    private LDatabaseHelper openHelper;

    private DatabaseManager() {
    }

    public synchronized void init(Context context) {
        if (this.context != null) {
            throw new RuntimeException("dao manager has been init");
        }
        this.context = context;
        openHelper = new LDatabaseHelper(this.context, LDatabaseHelper.NAME, LDatabaseHelper.VERSION);
    }

    public synchronized SQLiteDatabase getWritableDatabase() {
        return openHelper.getWritableDatabase();
    }

    public synchronized SQLiteDatabase getReadableDatabase() {
        return openHelper.getReadableDatabase();
    }

    public static DatabaseManager getInstance() {
        return INSTANCE.INSTANCE;
    }

    private static class INSTANCE {
        private static DatabaseManager INSTANCE = new DatabaseManager();
    }

}
