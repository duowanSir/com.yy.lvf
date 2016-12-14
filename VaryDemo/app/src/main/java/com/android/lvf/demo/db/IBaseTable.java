package com.android.lvf.demo.db;

/**
 * Created by slowergun on 2016/12/14.
 */
public interface IBaseTable<T> {
    String[] getValues();

    Class<?>[] getValueTypes();

    T newOne();
}
