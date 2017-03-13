package com.android.lvf.demo.db.dao;

import com.android.lvf.LLog;
import com.android.lvf.demo.db.AbstractDao;
import com.android.lvf.demo.db.table.VideoInfo;

import java.util.List;


/**
 * Created by slowergun on 2016/12/14.
 */
public class VideoInfoDao extends AbstractDao<VideoInfo> {
    public static final String TAG = VideoInfoDao.class.getSimpleName();
    public static final StringBuilder CREATE;

    static {
        if (VideoInfo.COLUMN_NAMES.length != VideoInfo.COLUMN_TYPES.length) {
            throw new RuntimeException("column isn't correspond to column types");
        }
        CREATE = new StringBuilder().append("CREATE TABLE IF NOT EXISTS")
                .append(" ")
                .append(VideoInfo.TABLE_NAME)
                .append(" ");
        for (int i = 0; i < VideoInfo.COLUMN_NAMES.length; i++) {
            if (i != 0) {
                CREATE.append(",");
            } else {
                CREATE.append("(");
            }
            CREATE.append("'").append(VideoInfo.COLUMN_NAMES[i]).append("'").append(" ").append(VideoInfo.COLUMN_TYPES[i]);
            if (i == VideoInfo.COLUMN_NAMES.length - 1) {
                CREATE.append(")");
            }
        }
        LLog.d(TAG, CREATE.toString());
    }

    @Override
    public String getTableName() {
        return VideoInfo.TABLE_NAME;
    }

    @Override
    public String[] getColumnNames() {
        return VideoInfo.COLUMN_NAMES;
    }

    @Override
    public String[] getColumnTypes() {
        return VideoInfo.COLUMN_TYPES;
    }

}
