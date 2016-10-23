package com.yy.lvf.transcoder;

import java.io.File;
import java.util.concurrent.Future;

import com.yy.lvf.FileChooser;
import com.yy.lvf.util.DynamicRecursiveFileObserver;
import com.yy.lvf.util.WxTimeLineVideoThief;
import com.yy.lvf.view.CustomUnitRulerView;
import com.yy.lvf.view.CustomUnitRulerView.Callback;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class TranscoderActivity extends Activity {
	private static final String	TAG					= "TranscoderActivity";
	private static final int	REQUEST_CODE_PICK	= 1;
	private Future<Void>		mFuture;
	private File				mRootD;
	private File				mOutputD;
	private CustomUnitRulerView	mRulerView;
	private TextView			mRulerValueTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transcoder);
		findViewById(R.id.select_video_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("video/*"), REQUEST_CODE_PICK);
			}
		});
		findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mFuture.cancel(true);
			}
		});

		mRootD = Environment.getExternalStorageDirectory();
		mOutputD = new File(mRootD, "outputs");
		if (!mOutputD.exists() || !mOutputD.isDirectory()) {
			mOutputD.mkdir();
		}

		mRulerValueTv = (TextView) findViewById(R.id.ruler_value_tv);
		mRulerView = (CustomUnitRulerView) findViewById(R.id.curv);
		mRulerView.setCallback(new Callback() {

			@Override
			public void slideCompleted(int value) {
				mRulerValueTv.setText("" + value);
			}

			@Override
			public void slide(int value) {
				mRulerValueTv.setText("" + value);
			}
		});
		mRulerView.setBoundary(240, 700);
		File root = Environment.getExternalStorageDirectory();
		File microMsg = new File(root, "tencent" + File.separator + "MicroMsg");
		DynamicRecursiveFileObserver.getInstance().setRoot(microMsg.getAbsolutePath());
		DynamicRecursiveFileObserver.getInstance().setCallback(new WxTimeLineVideoThief(new File(root, "Download" + File.separator + "video"), new File(root, "Download" + File.separator + "thumb")));
		DynamicRecursiveFileObserver.getInstance().start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_PICK: {
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				String inputPath = FileChooser.getAbsolutePathFromUri(this, uri);
				final File inputF = new File(inputPath);
				final File outputF = new File(mOutputD, inputF.getName());
				if (outputF.exists()) {
					outputF.delete();
				}
				new Thread() {
					public void run() {
						VideoTranscodeCore core = new VideoTranscodeCore();
						core.setFile(inputF, outputF);
						core.setCallback(new VideoTranscodeCore.Callback() {

							@Override
							public void transcoding(final int progress) {
								mRulerValueTv.post(new Runnable() {

									@Override
									public void run() {
										mRulerValueTv.setText("" + progress);
									}
								});
							}
						});
						//						core.syncTranscode();
						core.asyncTranscode();
					};
				}.start();
			}
			break;
		}
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.transcoder, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}