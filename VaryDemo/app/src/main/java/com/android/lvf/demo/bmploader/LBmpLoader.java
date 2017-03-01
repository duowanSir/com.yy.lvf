package com.android.lvf.demo.bmploader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.ImageView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by 烽 on 2017.2.28.
 */

public class LBmpLoader {
    private Context      context;
    private BmpRamCache  bmpRamCache;
    private BmpDiskCache bmpDiskCache;

    public void init(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * disk缓存id,也是文件名.*/
    public static String getBmpId(String bmpUrl) {
        if (TextUtils.isEmpty(bmpUrl)){
            return null;
        }
        try {
            byte[] bytes = bmpUrl.getBytes("utf-8");
            return Base64.encodeToString(bytes, Base64.URL_SAFE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ram缓存id,和sample有关,因为一张bitmap图可能会重采样成不同目标size.*/
    public static String getBmpId(String bmpUrl, int sample) {
        if (TextUtils.isEmpty(bmpUrl)){
            return null;
        }
        bmpUrl+=sample;
        try {
            byte[] bytes=bmpUrl.getBytes("utf-8");
            return Base64.encodeToString(bytes, Base64.URL_SAFE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap decodeBmpFromFile(String bmpUrl,String bmpLocalPath, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bmpLocalPath, options);
        int sample = calSample(options,reqWidth,reqHeight);
        options.inJustDecodeBounds=false;
        options.inSampleSize=sample;
        Bitmap bmp=BitmapFactory.decodeFile(bmpLocalPath,options);
        String bmpId=getBmpId(bmpUrl,sample);
        bmpRamCache.updateBmpRamCache(bmpId,bmp);
        return bmp;
    }

    public static int calSample(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int sample = 1;
        while ((width / sample) > reqWidth && (height / sample) > reqHeight) {
            sample *= 2;
        }
        return sample;
    }

    public void loadBmp(ImageView iv, String bmpUrl) {
        Bitmap bmp = getBmpFromRam(getBmpId(bmpUrl));
        if (bmp != null) {
            iv.setImageBitmap(bmp);
            return;
        }
        LoadBmpTask task = new LoadBmpTask(iv, bmpUrl);
        LBmpDrawable drawable = new LBmpDrawable(task);
        iv.setImageDrawable(drawable);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bmpUrl);
    }

    public static byte[] getBmpFromNet(String bmpUrl) {
        return null;
    }

    public static byte[] getBmpFromDisk(String bmpUrl) {
        return null;
    }

    public static Bitmap getBmpFromRam(String bmpId) {
        /**
         * 1.从ram获取bitmap
         * 2.更新ram*/
        return null;
    }

    public static LBmpLoader getInstance() {
        return INSTANCE.INSTANCE;
    }

    private static class INSTANCE {
        private static LBmpLoader INSTANCE = new LBmpLoader();
    }

    private static class BmpRamCache {
        private LinkedHashMap<String, Bitmap> bmpRamCache;// 使用LinkedHashMap一可以保证顺序,二方便插入;
        private Set<SoftReference<Bitmap>>    bmpReusableCache;// 从内存淘汰的bmp;

        public void updateBmpRamCache(String bmpId, Bitmap bmp) {
            /**
             * 1.无则添加至队列头,超出内存或者个数限制,则先移除队位;
             * 2.有则移动至队头*/
        }
    }

    private static class BmpDiskCache {
        private File bmpRootDir;
        private Set<String> bmpIdSet;


        public static void updataBmpDiskCache(String bmpId, byte[] bmpBytes) {
        }

    }

    private static class LoadBmpTask extends AsyncTask<String, Integer, byte[]> {
        /**
         * 异步任务的基本流程:
         * 1.从文件缓存取,无2,有4
         * 2.从网络数据取,无f,有3
         * 3.使用网络数据更新文件缓存,4
         * 4.在postExecute使用解析出来的数据更新内存缓存
         * 5.判断任务和widget是否一一对应,对应则展示,不对应则f
         */
        private WeakReference<ImageView> refImageView;
        private String                   bmpUrl;

        public LoadBmpTask(ImageView refImageView, String bmpUrl) {
            this.refImageView = new WeakReference<>(refImageView);
            this.bmpUrl = bmpUrl;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(byte[] bmpBytes) {
            super.onPostExecute(bmpBytes);
            if (refImageView.get() == null || !(refImageView.get().getDrawable() instanceof LBmpDrawable)) {
                return;
            }
            LBmpDrawable lBmpDrawable = (LBmpDrawable) refImageView.get().getDrawable();
            if (lBmpDrawable.getLoadBmpTask() == this) {
                Bitmap bmp = null /*= decodeBmpFromByte(bmpBytes, refImageView.get().getWidth(), refImageView.get().getHeight())*/;
                refImageView.get().setImageBitmap(bmp);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected byte[] doInBackground(String... params) {
            if (params == null || params.length < 1 || TextUtils.isEmpty(params[0])) {
                return null;
            }
            byte[] bmpBytes = getBmpFromDisk(params[0]);
            if (bmpBytes != null && bmpBytes.length > 0) {
                return bmpBytes;
            }
            bmpBytes = getBmpFromNet(params[0]);
            BmpDiskCache.updataBmpDiskCache(getBmpId(params[0]), bmpBytes);
            return bmpBytes;
        }
    }

    private static class LBmpDrawable extends BitmapDrawable {
        private LoadBmpTask loadBmpTask;

        public LBmpDrawable(LoadBmpTask loadBmpTask) {
            this.loadBmpTask = loadBmpTask;
        }

        public LoadBmpTask getLoadBmpTask() {
            return loadBmpTask;
        }
    }
}
