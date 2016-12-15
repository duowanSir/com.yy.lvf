package com.android.lvf.demo.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.android.lvf.LLog;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by slowergun on 2016/12/14.
 */
public abstract class AbstractDao<T extends IBaseTable> {
    private static final String TAG = AbstractDao.class.getSimpleName();
    protected SQLiteOpenHelper mOpenHelper;
    protected String           mPrimaryKey;

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
        String sqlInsert = getInsertStr(object, conflict);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL(sqlInsert, object.getColumn2Value().values().toArray());
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return true;
    }

    public boolean insert(List<T> object) {

        return false;
    }

    public boolean delete(T object) {
        return false;
    }

    public boolean delete(List<T> objects) {
        return false;
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
        String arg = getRetrieveStr(object);
        Cursor cursor = null;
        Class<?>[] valueTypes = null;
//        Cursor cursor = mDatabase.rawQuery(arg, object.getValues());
//        Class<?>[] valueTypes = object.getValueTypes();
//
        List<T> result = null;
        try {
            while (cursor.moveToNext()) {
//                T ele = (T) object.newOne();
                T ele = null;
                Object[] columnValues = new Object[valueTypes.length];
                for (int i = 0; i < valueTypes.length; i++) {
                    if (valueTypes[i] == Double.TYPE) {
                        columnValues[i] = cursor.getDouble(i);
                    } else if (valueTypes[i] == Float.TYPE) {
                        columnValues[i] = cursor.getFloat(i);
                    } else if (valueTypes[i] == Integer.TYPE) {
                        columnValues[i] = cursor.getInt(i);
                    } else if (valueTypes[i] == String.class) {
                        columnValues[i] = cursor.getString(i);
                    } else if (valueTypes[i] == Short.TYPE) {
                        columnValues[i] = cursor.getShort(i);
                    } else if (valueTypes[i] == Integer.TYPE) {
                        columnValues[i] = cursor.getLong(i);
                    } else if (valueTypes[i] == Integer.TYPE) {
                        columnValues[i] = cursor.getBlob(i);
                    }
                }
                // 设置值
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(ele);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    protected String getRetrieveStr(T object) {
        if (object == null) {
            return "select * from " + getTableName();
        }
        Object[] columnValues = null;
//        Object[] columnValues = object.getValues();
        if (columnValues == null) {
            throw new RuntimeException("base table entity must correct initial column value field");
        }
        if (columnValues.length != getColumnNames().length) {
            throw new RuntimeException("column value count must correspond to column name count");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("select * from ")
                .append(getTableName());
        boolean isFirst = true;
        for (int i = 0; i < columnValues.length; i++) {
            if (columnValues[i] != null) {
                if (isFirst) {
                    sb.append(" where ");
                }
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(" & ");
                }
                sb.append(getColumnNames()[i]).append(" = ").append("?");
            }
        }
        return sb.toString();
    }

    protected String getInsertStr(T object, String conflictAlgorithm) {
        if (object == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder valueSb = new StringBuilder();
        sb.append("INSERT ");
        if (!TextUtils.isEmpty(conflictAlgorithm)) {
            sb.append(conflictAlgorithm);
        }
        sb.append(" INTO ")
        .append(getTableName());
        sb.append(" ");
        Set<Map.Entry<String, Object>> entries = object.getColumn2Value().entrySet();
        Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
        boolean fistNotNullValue = false;
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (!fistNotNullValue) {
                fistNotNullValue = true;
                sb.append("(");
                valueSb.append("(");
            } else {
                sb.append(",");
                valueSb.append(",");
            }
            sb.append(entry.getKey());
            valueSb.append("?");
        }
        sb.append(") VALUES ").append(valueSb).append(")");
        LLog.d(TAG, sb.toString());
        return sb.toString();
    }

}
