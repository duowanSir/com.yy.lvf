package com.android.lvf.demo.db.dao;

import com.android.lvf.demo.db.AbstractDao;
import com.android.lvf.demo.db.table.VideoInfo;

/**
 * Created by slowergun on 2016/12/14.
 */
public class VideoInfoDao extends AbstractDao<VideoInfo> {
    public static final String   TAG          = VideoInfo.class.getSimpleName();
    public static final String[] COLUMN_NAMES = {"OId", "ServerTimestampS", "ClientTimestampS", "VisibleTimeS", "Uploaded"};
    public static final String[] COLUMN_TYPES = {"INTEGER PRIMARY KEY", "INTEGER", "INTEGER", "INTEGER", "INTEGER"};
    public static final StringBuilder CREATE;

    static {
        if (COLUMN_NAMES.length != COLUMN_TYPES.length) {
            throw new RuntimeException("column isn't correspond to column types");
        }
        CREATE = new StringBuilder().append("CREATE TABLE IF NOT EXISTS")
                .append(" ")
                .append(TAG)
                .append(" ");
        for (int i = 0; i < COLUMN_NAMES.length; i++) {
            if (i != 0) {
                CREATE.append(",");
            } else {
                CREATE.append("(");
            }
            CREATE.append("'").append(COLUMN_NAMES[i]).append("'").append(" ").append(COLUMN_TYPES[i]);
            if (i == COLUMN_NAMES.length - 1) {
                CREATE.append(")");
            }
        }
    }

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public String[] getColumnNames() {
        return new String[0];
    }

    @Override
    public String[] getColumnTypes() {
        return new String[0];
    }

    @Override
    public void createTable() {

    }
}
