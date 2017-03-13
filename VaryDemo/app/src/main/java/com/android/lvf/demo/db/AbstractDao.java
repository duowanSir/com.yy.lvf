package com.android.lvf.demo.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.android.lvf.LLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by slowergun on 2016/12/14.
 */
public abstract class AbstractDao<T extends IBaseTable> {
    private static final String                 TAG               = AbstractDao.class.getSimpleName();
    private static final int                    TRY_LOCK_TIME_OUT = 100;
    private final        ReentrantReadWriteLock readWriteLock     = new ReentrantReadWriteLock();

    protected String mPrimaryKey;

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
            LLog.d(TAG, "插入语句为空");
            return false;
        }
        SQLiteDatabase database = DatabaseManager.getInstance().getWritableDatabase();
        try {
            if (readWriteLock.writeLock().tryLock(TRY_LOCK_TIME_OUT, TimeUnit.MILLISECONDS)) {
                try {
                    readWriteLock.writeLock().lock();
                    database.beginTransaction();
                    database.execSQL(sqlInsert, object.getColumnIndex2Value().values().toArray());
                    database.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    database.endTransaction();
                    readWriteLock.writeLock().unlock();
                }
                return true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 不支持分号隔开的数据库语句
     */
    public boolean insert(List<T> objects, String conflict) {
        if (objects == null || objects.isEmpty()) {
            return false;
        }
        SQLiteDatabase database = DatabaseManager.getInstance().getWritableDatabase();
        try {
            if (readWriteLock.writeLock().tryLock(TRY_LOCK_TIME_OUT, TimeUnit.MILLISECONDS)) {
                try {
                    readWriteLock.writeLock().lock();
                    database.beginTransaction();
                    for (int i = 0; i < objects.size(); i++) {
                        T j = objects.get(i);
                        String sqlInsert = getSqlInsert(j, conflict);
                        if (TextUtils.isEmpty(sqlInsert)) {
                            continue;
                        }
                        database.execSQL(sqlInsert, j.getColumnIndex2Value().values().toArray());
                    }
                    database.setTransactionSuccessful();
                } finally {
                    readWriteLock.writeLock().unlock();
                    database.endTransaction();
                }
                return true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(T object) {
        String sqlDelete = getSqlDelete(object);
        if (TextUtils.isEmpty(sqlDelete)) {
            return false;
        }
        SQLiteDatabase database = DatabaseManager.getInstance().getWritableDatabase();
        try {
            if (readWriteLock.writeLock().tryLock(TRY_LOCK_TIME_OUT, TimeUnit.MILLISECONDS)) {
                try {
                    readWriteLock.writeLock().lock();
                    database.beginTransaction();
                    database.execSQL(sqlDelete, object.getColumnIndex2Value().values().toArray());
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                    readWriteLock.writeLock().unlock();
                }
                return true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(List<T> tObjects) {
        if (tObjects == null || tObjects.isEmpty()) {
            return false;
        }
        SQLiteDatabase database = DatabaseManager.getInstance().getWritableDatabase();
        try {
            if (readWriteLock.writeLock().tryLock(TRY_LOCK_TIME_OUT, TimeUnit.MILLISECONDS)) {
                try {
                    readWriteLock.writeLock().lock();
                    database.beginTransaction();
                    for (int i = 0; i < tObjects.size(); i++) {
                        T j = tObjects.get(i);
                        String sqlDelete = getSqlDelete(j);
                        if (TextUtils.isEmpty(sqlDelete)) {
                            continue;
                        }
                        database.execSQL(sqlDelete, j.getColumnIndex2Value().values().toArray());
                    }
                    database.setTransactionSuccessful();
                    return true;
                } finally {
                    database.endTransaction();
                    readWriteLock.writeLock().unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean update(T object) {
        return false;
    }

    public boolean update(List<T> object) {
        return false;
    }

    public List<T> retrieve(T object) {
        String sqlRetrieve = getSqlRetrieve(object);
        String[] selectionArgs = null;
        if (!object.getColumnIndex2Value().isEmpty()) {
            selectionArgs = new String[]{String.valueOf(object.getPrimaryValue())};
        }
        Cursor cursor = null;
        SQLiteDatabase database=DatabaseManager.getInstance().getReadableDatabase();
        try {
            if (readWriteLock.readLock().tryLock(TRY_LOCK_TIME_OUT,TimeUnit.MILLISECONDS)){
                try{
                    readWriteLock.readLock().lock();
                    database.beginTransaction();
                    cursor=database.rawQuery(sqlRetrieve,selectionArgs);
                    database.setTransactionSuccessful();
                }finally {
                    database.endTransaction();
                    readWriteLock.readLock().unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LinkedHashMap<Integer, Class<?>> i2t = object.getColumnIndex2Type();
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
        } catch (Exception e) {
            e.printStackTrace();
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
            } else {
                sqlDel.append(" AND ");
            }
            firstCondition = false;
            sqlDel.append(getColumnNames()[entry.getKey()]).
                    append(" = ").
                    append("?");
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
        LinkedHashMap<Integer, Object> i2v = object.getColumnIndex2Value();
        if (object == null || i2v.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

}
