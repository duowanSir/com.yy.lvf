package com.android.lvf.demo.db;

import java.util.LinkedHashMap;

/**
 * Created by slowergun on 2016/12/14.
 */
public interface IBaseTable {
    LinkedHashMap<Integer, Class<?>> getColumnIndex2Type();

    LinkedHashMap<Integer, Object> getColumnIndex2Value();

    Long getPrimaryValue();

    void putByColumnIndex2Value(int index, Object value);

    IBaseTable create();
}
