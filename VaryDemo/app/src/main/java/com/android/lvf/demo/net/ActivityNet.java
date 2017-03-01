package com.android.lvf.demo.net;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.lvf.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by 烽 on 2017/2/1.
 */

public class ActivityNet extends Activity implements View.OnClickListener {
    private TextView mShowMessageTv;
    private Button   mSendMessageBtn;
    private Executor mExecutor;

    private int mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net);

        mShowMessageTv = (TextView) findViewById(R.id.show_message_tv);
        mSendMessageBtn = (Button) findViewById(R.id.send_message_btn);

        mSendMessageBtn.setOnClickListener(this);

        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onClick(View v) {
        if (v == mSendMessageBtn) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    oneHttp(String.valueOf(mContent++));
                }
            });
        }
    }

    private void oneHttp(String str) {
        URL url = null;
        try {
            url = new URL("http://192.168.137.1");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            url = null;
        }
        if (url == null) {
            return;
        }
        HttpURLConnection httpURLConnection = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.connect();

            bos = new BufferedOutputStream(httpURLConnection.getOutputStream());
            byte[] bytes = str.getBytes();
            bos.write(bytes, 0, bytes.length);
            bos.write(bytes, 0, bytes.length);
            bos.flush();

            bis = new BufferedInputStream(httpURLConnection.getInputStream());
            byte[] cache = new byte[1024];
            int size = 0;
            final StringBuilder sb = new StringBuilder();
            while ((size = bis.read(cache, 0, 1024)) != -1) {
                sb.append(new String(cache, 0, size));
            }

            mShowMessageTv.post(new Runnable() {
                @Override
                public void run() {
                    if (TextUtils.isEmpty(mShowMessageTv.getText())) {
                        mShowMessageTv.setText("" + sb);
                    }
                    mShowMessageTv.setText(mShowMessageTv.getText() + sb.toString());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

    }
}
