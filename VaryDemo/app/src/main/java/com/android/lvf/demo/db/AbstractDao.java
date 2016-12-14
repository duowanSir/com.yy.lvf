package com.android.lvf.demo.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slowergun on 2016/12/14.
 */
public abstract class AbstractDao<T extends IBaseTable> {
    private   SQLiteDatabase mDatabase;
    protected String         mPrimaryKey;

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

    public abstract void createTable();

    public boolean insert(T object) {

        return false;
    }

    public boolean insert(T object, String conflict) {

        return false;
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
        Cursor cursor = mDatabase.rawQuery(arg, object.getValues());
        Class<?>[] valueTypes = object.getValueTypes();
        List<T> result = null;
        try {
            while (cursor.moveToNext()) {
                T ele = (T) object.newOne();
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
        Object[] columnValues = object.getValues();
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

    protected String getInsertStr(T object, String conflict) {
        if (object == null) {
            return null;
        }

    }


    private SQLiteDatabase getDatabase() {
        return null;
    }

}
