package com.android.lvf;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LLog {
    /**
     * 日志级别
     */
    public enum Level {
        VERBOSE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4), ASSERT(5);
        int value;

        private Level(int value) {
            this.value = value;
        }
    }

    private final static String  TAG    = LLog.class.getSimpleName();
    /**
     * 全局日志开关，默认为开启
     */
    private static       boolean enable = true;
    /**
     * 日志保存目录，默认为null,将不会进行保存
     */
    private static File saveDir;                                                                            // 日志保存的目录
    //	private static File saveFile;
    /**
     * 文件保存的日志级别默认为信息（即INFO）
     */
    private static Level fileLevel = Level.INFO;

    /**
     * 控制台输出的日志级别，开发环境默认为VERBOSE，生产环境的默认为ERROR
     */
    private static Level               consoleLevel     = Level.VERBOSE;
    //	static {
    //		consoleLevel = BuildConfig.DEBUG ? Level.VERBOSE : Level.ERROR;
    //	}
    /**
     * 用于换成是否可以输出的判断结果，提高性能
     */
    private static Map<Level, Boolean> consoleEnableMap = Collections.synchronizedMap(new HashMap<Level, Boolean>());
    private static Map<Level, Boolean> fileEnableMap    = Collections.synchronizedMap(new HashMap<Level, Boolean>());
    private static SimpleDateFormat    dateFormat       = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 全局控制是否输出日志，若为false,日志则不会输出到控制台和文件
     *
     * @param enable
     */
    public static void setEnable(boolean enable) {
        LLog.enable = enable;
        consoleEnableMap.clear();
        fileEnableMap.clear();
    }

    /**
     * 设置相关日志级别，输出的日志的级别等于或高于相应的设置才会输出
     *
     * @param debugLevel
     *            开发环境的日志级别
     * @param productLevel
     *            生产环境的日志级别
     * @param fileLevel
     *            保持到文件的日志级别（开发和生产环境都一样）
     */
    //	public static void setLevel(Level debugLevel, Level productLevel, Level fileLevel) {
    //		LLog.consoleLevel = BuildConfig.DEBUG ? debugLevel : productLevel;
    //		BoxLog.fileLevel = fileLevel;
    //		consoleEnableMap.clear();
    //		fileEnableMap.clear();
    //	}

    /**
     * 设置保存日志文件的目录
     *
     * @param file 保存目录
     * @return 设置成功返回true, 否则返回false（file为null或非目录或相应目录不存在时创建失败）
     */
    public static boolean setSaveDir(File file) {
        boolean res = false;
        if (file == null || !file.isDirectory()) {
            res = false;
        }
        res = file.exists() ? true : file.mkdirs();
        if (res) {
            saveDir = file;
            consoleEnableMap.clear();
            fileEnableMap.clear();
        }
        return res;
    }

    /**
     * 是否将当前日志打印到控制台
     *
     * @param currLevel
     * @return
     */
    private static boolean shouldConsoleOutput(Level currLevel) {
        Boolean res = consoleEnableMap.get(currLevel);
        if (res == null) {
            if (enable) {
                res = currLevel.value >= consoleLevel.value ? true : false;
            } else {
                res = false;
            }
            consoleEnableMap.put(currLevel, res);
        }
        return res;
    }

    /**
     * 是否将当前日志打印到文件
     *
     * @param currLevel
     * @return
     */
    private static boolean shouldFileOutput(Level currLevel) {
        Boolean res = fileEnableMap.get(currLevel);
        if (res == null) {
            if (!enable || saveDir == null || !saveDir.exists()) {
                res = false;
            } else {
                res = currLevel.value >= fileLevel.value ? true : false;
            }
            fileEnableMap.put(currLevel, res);
        }
        return res;
    }

    private static StringBuilder warpMsg(String msg, Throwable tr) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement ste = new Throwable().getStackTrace()[3];
        sb.append("[").append(ste.getFileName()).append(":").append(ste.getLineNumber()).append("] ");
        if (msg != null) {
            sb.append(msg);
        }
        if (tr != null) {
            sb.append(" [exception@").append(tr.getStackTrace()[0].getFileName()).append(":");
            sb.append(tr.getStackTrace()[0].getLineNumber()).append("] ").append(tr.toString());
        }
        return sb;
    }

    private static void printLog(Level level, Object tag, String msg, Throwable tr) {
        StringBuilder sb = null;
        String tag1 = null;
        if (tag == null) {
            tag1 = TAG;
        } else if (tag instanceof String) {
            tag1 = (String) tag;
        } else {
            tag1 = tag.getClass().getSimpleName();
        }
        if (shouldConsoleOutput(level)) {
            sb = warpMsg(msg, tr);
            switch (level) {
                case ASSERT:
                    android.util.Log.wtf(tag1, sb.toString());
                    break;
                case DEBUG:
                    android.util.Log.d(tag1, sb.toString());
                    break;
                case ERROR:
                    android.util.Log.e(tag1, sb.toString());
                    break;
                case INFO:
                    android.util.Log.i(tag1, sb.toString());
                    break;
                case VERBOSE:
                    android.util.Log.v(tag1, sb.toString());
                    break;
                case WARN:
                    android.util.Log.w(tag1, sb.toString());
                    break;
            }
        }
        if (shouldFileOutput(level)) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(dateFormat.format(new Date())).append("\t").append(level).append("\t").append(tag1);
            if (sb == null) {
                sb = warpMsg(msg, tr);
            }
            sb2.append("\t").append(sb);
        }
    }

    public static void d(Object tag, String msg) {
        printLog(Level.DEBUG, tag, msg, null);
    }

    public static void d(Object tag, String msg, Throwable tr) {
        printLog(Level.DEBUG, tag, msg, tr);
    }

    public static void e(Object tag, String msg) {
        printLog(Level.ERROR, tag, msg, null);
    }

    public static void e(Object tag, Throwable tr) {
        printLog(Level.ERROR, tag, null, tr);
    }

    public static void e(Object tag, String msg, Throwable tr) {
        printLog(Level.ERROR, tag, msg, tr);
    }

    public static void i(Object tag, String msg) {
        printLog(Level.INFO, tag, msg, null);
    }

    public static void i(Object tag, String msg, Throwable tr) {
        printLog(Level.INFO, tag, msg, tr);
    }

    public static void v(Object tag, String msg) {
        printLog(Level.VERBOSE, tag, msg, null);
    }

    public static void v(Object tag, String msg, Throwable tr) {
        printLog(Level.VERBOSE, tag, msg, tr);
    }

    public static void w(Object tag, Throwable tr) {
        printLog(Level.WARN, tag, null, tr);
    }

    public static void w(Object tag, String msg, Throwable tr) {
        printLog(Level.WARN, tag, msg, tr);
    }

    public static void w(Object tag, String msg) {
        printLog(Level.WARN, tag, msg, null);
    }

    public static void wtf(Object tag, Throwable tr) {
        printLog(Level.ASSERT, tag, null, tr);
    }

    public static void wtf(Object tag, String msg, Throwable tr) {
        printLog(Level.ASSERT, tag, msg, tr);
    }

    public static void wtf(Object tag, String msg) {
        printLog(Level.ASSERT, tag, msg, null);
    }


    public static void printThreadStacks(String tag, String keyword) {
        printStackTraces(Thread.currentThread().getStackTrace(), tag, keyword);
    }

    private static void printStackTraces(StackTraceElement[] traces, String tag, String keyword) {

        printLog(Level.DEBUG, tag, "------------------------------------", null);
        for (StackTraceElement e : traces) {
            String info = e.toString();
//            if (info.indexOf(keyword) != -1) {
                printLog(Level.DEBUG, tag, info, null);
//            }
        }
        printLog(Level.DEBUG, tag, "------------------------------------", null);
    }

}
