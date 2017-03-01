package com.android.lvf.demo.imageloader;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * Created by slowergun on 2017.02.28.
 */

public class LImageLoader {
    private Context context;
    private File bmpDiskCache;

    public void init(Context context) {
        this.context = context.getApplicationContext();
    }

    public static LImageLoader getInstance() {
        return Instance.INSTANCE;
    }

    private static class Instance {
        private static LImageLoader INSTANCE = new LImageLoader();
    }

    public static class BmpRamCache {
        private LinkedHashMap<String, Bitmap> bmpRamCache;
        private long maxMemorySize;
        private int maxSize;
    }
}
