package com.yy.lvf;

import java.io.File;

import com.yy.lvf.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.widget.TextView;

public class MainActivity extends Activity {
    class MyFileObserver extends FileObserver {

	public MyFileObserver(String path) {
	    super(path);
	}

	@Override
	public void onEvent(int event, String path) {
	    if (event == FileObserver.CLOSE_WRITE && path.endsWith(".mp4")) {
		// System.out.println(event + " " + path);
		Message msg = handler.obtainMessage();
		msg.what = 0x1;
		msg.obj = path;
		handler.sendMessage(msg);
	    }
	}
    }

    String wxTimeLineVideoPath = null;
    TextView previewTv;
    MyFileObserver fileObserver;

    HandlerThread ht;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	previewTv = (TextView) findViewById(R.id.preview_tv);
	wxTimeLineVideoPath = getWxTimeLineVideoFile().getAbsolutePath();
	previewTv.setText(wxTimeLineVideoPath);
	fileObserver = new MyFileObserver(wxTimeLineVideoPath);
	fileObserver.startWatching();

	ht = new HandlerThread("not_ui_thread", Process.THREAD_PRIORITY_BACKGROUND);
	ht.start();

	handler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
		super.handleMessage(msg);
		String path = (String) msg.obj;
		switch (msg.what) {
		case 0x1:
		    File file = new File(wxTimeLineVideoPath, "dst.mp4");
		    file.renameTo(new File(wxTimeLineVideoPath, path));
		default:
		    break;
		}
	    }
	};
    }

    protected void onDestroy() {
	fileObserver.stopWatching();
    };

    File getWxTimeLineVideoFile() {
	File parent = new File(Environment.getExternalStorageDirectory(), "tencent");
	File[] children = null;

	if (parent.exists() && parent.isDirectory()) {
	    children = parent.listFiles();
	    for (File child : children) {
		if ("micromsg".equalsIgnoreCase(child.getName()) && child.isDirectory()) {
		    parent = child;
		    break;
		}
	    }

	    children = parent.listFiles();
	    for (File child : children) {
		if (child.isDirectory()) {
		    int flag = 0;
		    for (File file : child.listFiles()) {
			if (child.isDirectory()) {
			    if ("draft".equalsIgnoreCase(file.getName())) {
				flag++;
			    } else if ("video".equalsIgnoreCase(file.getName())) {
				flag++;
				parent = file;
			    }
			}
		    }
		    if (flag != 2) {
			parent = null;
		    } else {
			break;
		    }
		}
	    }
	}
	return parent;
    }
}