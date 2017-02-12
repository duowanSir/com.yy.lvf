package com.android.lvf.demo.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.android.lvf.LLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by slowergun on 2016/12/14.
 */
public abstract class AbstractDao<T extends IBaseTable> {
    private static final String TAG = AbstractDao.class.getSimpleName();
    private static SQLiteDatabase   DATABASE_INSTANCE;
    protected      String           mPrimaryKey;

    public static void setDatabaseInstance(SQLiteDatabase instance) {
        DATABASE_INSTANCE = instance;
    }

    public abstract String getTableName();

    public abstract String[] getColumnNames();

    public abstract String[] getColumnTypes();

    protected String getPrimaryKey() {
        if (!TextUtils.isEmpty(mPrimaryKey)) {
            return mPrimaryKey;
        } else {
            for (int i = 0; i < getColumnTypes().length; i++) {
                String type = getColumnTypes()[i];
                if (type.toUpperCase().contains("PRIMARY KEY")) {
                    return getColumnNames()[i];
                }
            }
        }
        return null;
    }

    public boolean insert(T object) {
        return insert(object, "OR REPLACE");
    }

    public boolean insert(T object, String conflict) {
        String sqlInsert = getSqlInsert(object, conflict);
        if (TextUtils.isEmpty(sqlInsert)) {
            return false;
        }
        if (DATABASE_INSTANCE == null) {
            return false;
        }
        synchronized (DATABASE_INSTANCE) {
            try {
                DATABASE_INSTANCE.beginTransaction();
                DATABASE_INSTANCE.execSQL(sqlInsert, object.getColumnIndex2Value().values().toArray());
                DATABASE_INSTANCE.setTransactionSuccessful();
            } finally {
                DATABASE_INSTANCE.endTransaction();
                DATABASE_INSTANCE.close();
            }
        }
        return true;
    }

    public boolean insert(List<T> objects, String conflict) {
        if (DATABASE_INSTANCE == null || objects == null || objects.isEmpty()) {
            return false;
        }
        synchronized (DATABASE_INSTANCE) {
            try {
                DATABASE_INSTANCE.beginTransaction();
                for (T each : objects) {
                    String sqlInsert = getSqlInsert(each, conflict);
                    if (!TextUtils.isEmpty(sqlInsert)) {
                        DATABASE_INSTANCE.execSQL(sqlInsert, each.getColumnIndex2Value().values().toArray());
                    }
                }
                DATABASE_INSTANCE.setTransactionSuccessful();
            } finally {
                DATABASE_INSTANCE.endTransaction();
                DATABASE_INSTANCE.close();
            }
        }
        return true;
    }

    public boolean delete(T object) {
        if (DATABASE_INSTANCE == null) {
            return false;
        }
        String sqlDelete = getSqlDelete(object);
        if (TextUtils.isEmpty(sqlDelete)) {
            return false;
        }
        synchronized (DATABASE_INSTANCE) {
            try {
                DATABASE_INSTANCE.beginTransaction();
                DATABASE_INSTANCE.execSQL(sqlDelete, object.getColumnIndex2Value().values().toArray());
                DATABASE_INSTANCE.setTransactionSuccessful();
            } finally {
                DATABASE_INSTANCE.endTransaction();
                DATABASE_INSTANCE.close();
            }
        }
        return true;
    }

    public boolean delete(List<T> objects) {
        if (DATABASE_INSTANCE == null || objects == null || objects.isEmpty()) {
            return false;
        }
        synchronized (DATABASE_INSTANCE) {
            try {
                DATABASE_INSTANCE.beginTransaction();
                for (T each:objects
                        ) {
                    String sqlDelete = getSqlDelete(each);
                    if (!TextUtils.isEmpty(sqlDelete)) {
                        DATABASE_INSTANCE.execSQL(sqlDelete, each.getColumnIndex2Value().values().toArray());
                    }
                }
                DATABASE_INSTANCE.setTransactionSuccessful();
            } finally {
                DATABASE_INSTANCE.endTransaction();
                DATABASE_INSTANCE.close();
            }
        }
        return true;
    }

    public boolean update(T object) {
        return false;
    }

    public boolean update(List<T> object) {
        return false;
    }

    public List<T> retrieve(T object) {
        if (object == null) {
            return null;
        }
        String sqlRetrieve = getSqlRetrieve(object);
//        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String[] selectionArgs = null;
        if (!object.getColumnIndex2Value().isEmpty()) {
            selectionArgs = new String[]{String.valueOf(object.getColumnIndex2Value().get(getPrimaryKey()))};
        }
        Cursor cursor = DATABASE_INSTANCE.rawQuery(sqlRetrieve, selectionArgs);
        LinkedHashMap<Integer, Class<?>> i2t = object.getColumnIndex2Type();
        if (i2t.isEmpty()) {
            throw new RuntimeException("index 2 type correspond should be implemented first");
        }
        List<T> result = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                T ele = (T) object.create();
                Iterator<Map.Entry<Integer, Class<?>>> iterator = i2t.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, Class<?>> entry = iterator.next();
                    Integer index = entry.getKey();
                    Class<?> type = entry.getValue();
                    if (type == Double.class) {
                        ele.putByColumnIndex2Value(index, cursor.getDouble(index));
                    } else if (type == Float.class) {
                        ele.putByColumnIndex2Value(index, cursor.getFloat(index));
                    } else if (type == Integer.class) {
                        ele.putByColumnIndex2Value(index, cursor.getInt(index));
                    } else if (type == Long.class) {
                        ele.putByColumnIndex2Value(index, cursor.getLong(index));
                    } else if (type == Short.class) {
                        ele.putByColumnIndex2Value(index, cursor.getShort(index));
                    } else if (type == String.class) {
                        ele.putByColumnIndex2Value(index, cursor.getString(index));
                    }
                }
                result.add(ele);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    protected String getSqlInsert(T object, String conflictAlgorithm) {
        if (object == null) {
            return null;
        }
        LinkedHashMap<Integer, Object> i2v = object.getColumnIndex2Value();
        if (i2v.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder valueSb = new StringBuilder();
        sb.append("INSERT ");
        if (!TextUtils.isEmpty(conflictAlgorithm)) {
            sb.append(conflictAlgorithm);
        }
        sb.append(" INTO ").append(getTableName());
        Iterator<Map.Entry<Integer, Object>> iterator = i2v.entrySet().iterator();
        boolean firstColumn = true;
        while (iterator.hasNext()) {
            Map.Entry<Integer, Object> entry = iterator.next();
            if (firstColumn) {
                sb.append("(");
                valueSb.append("(");
                firstColumn = false;
            } else {
                sb.append(",");
                valueSb.append(",");
            }
            sb.append(getColumnNames()[entry.getKey()]);
            valueSb.append("?");
        }
        sb.append(") VALUES ").append(valueSb).append(")");
        LLog.d(TAG, sb.toString());
        return sb.toString();
    }

    protected String getSqlDelete(T object) {
        if (object == null) {
            return null;
        }
        LinkedHashMap<Integer, Object> i2v = object.getColumnIndex2Value();
        if (i2v.isEmpty()) {
            return null;
        }
        StringBuilder sqlDel = new StringBuilder();
        sqlDel.append("DELETE FROM " + getTableName());
        Iterator<Map.Entry<Integer, Object>> iterator = i2v.entrySet().iterator();
        boolean firstCondition = true;
        while (iterator.hasNext()) {
            Map.Entry<Integer, Object> entry = iterator.next();
            if (firstCondition) {
                sqlDel.append(" WHERE ");
            }
            sqlDel.append(getColumnNames()[entry.getKey()]).
                    append(" = ").
                    append("?");
            if (!firstCondition) {
                sqlDel.append(" AND ");
            }
        }
        LLog.d(TAG, sqlDel.toString());
        return sqlDel.toString();
    }

    protected String getSqlRetrieve(T object) {
        if (object == null || object.getColumnIndex2Value().isEmpty()) {
            return "SELECT * FROM " + getTableName();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").
                append(getTableName()).
                append(" WHERE ").
                append(getPrimaryKey()).
                append(" = ?");
        return sb.toString();
    }

    protected String getSqlUpdate(T object) {
        if (object == null || object.getPrimaryValue() == null) {
            return null;
        }
        LinkedHashMap<Integer, Object> i2v = object.getColumnIndex2Value();
        if (object == null || i2v.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
//        sb.append("UPDATE ").append("SET ")
        return sb.toString();
    }

}
