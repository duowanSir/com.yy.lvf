package com.yy.lvf.transcoder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

import com.yy.lvf.FileChooser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class TranscoderActivity extends Activity {
	private static final String TAG = "TranscoderActivity";
	private static final String FILE_PROVIDER_AUTHORITY = "net.ypresto.androidtranscoder.example.fileprovider";
	private static final int REQUEST_CODE_PICK = 1;
	private static final int PROGRESS_BAR_MAX = 1000;
	private Future<Void> mFuture;
	private File mRootD;
	private File mOutputD;

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
		mOutputD = new File(mRootD, "jutput");
		if (!mOutputD.exists() || !mOutputD.isDirectory()) {
			mOutputD.mkdir();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_PICK: {
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				String inputPath = FileChooser.getAbsolutePathFromUri(this, uri);
				File inputF = new File(inputPath);
				ExtractDecodeEditEncodeMuxTest engine = new ExtractDecodeEditEncodeMuxTest();
				File outputF = new File(mOutputD, inputF.getName());
				if (!outputF.exists() || !outputF.isFile()) {
					try {
						outputF.createNewFile();
						engine.testExtractDecodeEditEncodeMuxAudioVideo(inputF, outputF);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
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

	private void onTranscodeFinished(boolean isSuccess, String toastMessage, ParcelFileDescriptor parcelFileDescriptor) {
		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
		progressBar.setIndeterminate(false);
		progressBar.setProgress(isSuccess ? PROGRESS_BAR_MAX : 0);
		switchButtonEnabled(false);
		Toast.makeText(TranscoderActivity.this, toastMessage, Toast.LENGTH_LONG).show();
		try {
			parcelFileDescriptor.close();
		} catch (IOException e) {
			Log.w("Error while closing", e);
		}
	}

	private void switchButtonEnabled(boolean isProgress) {
		findViewById(R.id.select_video_button).setEnabled(!isProgress);
		findViewById(R.id.cancel_button).setEnabled(isProgress);
	}

}