package com.android.lvf.demo.bmploader;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by 烽 on 2017.2.28.
 */

public class LBmpLoader {

    private static class BmpRamCache{

    }

    private static class BmpDiskCache{

    }

    public static Bitmap decodeBmpFromByte(byte[] bytes,int reqWidth,int reqHeight){
        return null;
    }

    public Bitmap getBmpFromRam(){
        return null;
    }

    public static byte[] getBmpFromNet(String bmpUrl){
        return null;
    }

    public static byte[] getBmpFromDisk(String bmpUrl){
        return null;
    }

    private static class LoadBmpTask extends AsyncTask<String,Integer,Bitmap> {
        private WeakReference<ImageView> refImageView;
        private String bmpUrl;

        public LoadBmpTask(ImageView refImageView,String bmpUrl){
            this.refImageView = new WeakReference<>(refImageView);
            this.bmpUrl=bmpUrl;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (refImageView.get() == null || !(refImageView.get().getDrawable() instanceof LBmpDrawable)){
                return;
            }
            LBmpDrawable lBmpDrawable = (LBmpDrawable) refImageView.get().getDrawable();
            if (lBmpDrawable.getLoadBmpTask() == this) {
                refImageView.get().setImageBitmap(bitmap);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            if (params == null || params.length<1|| TextUtils.isEmpty(params[0])) {
                return null;
            }
            byte[] bmpBytes = getBmpFromDisk(params[0]);

            if (bmpBytes != null) {
                return decodeBmpFromByte(bmpBytes,)
            }
            return null;
        }
    }

    private static class LBmpDrawable extends BitmapDrawable {
        private LoadBmpTask loadBmpTask;

        public LBmpDrawable(LoadBmpTask loadBmpTask){
            this.loadBmpTask=loadBmpTask;
        }

        public LoadBmpTask getLoadBmpTask() {
            return loadBmpTask;
        }
    }
}
