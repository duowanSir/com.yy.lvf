package com.yy.lvf.transcoder;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Future;

import com.yy.lvf.FileChooser;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import net.ypresto.androidtranscoder.MediaTranscoder;
import net.ypresto.androidtranscoder.format.CompressFileSizeFormatStrategy;
import net.ypresto.androidtranscoder.format.MediaFormatStrategyPresets;

public class TranscoderActivity extends Activity {
	private static final String TAG = "TranscoderActivity";
	private static final int REQUEST_CODE_PICK = 1;
	private static final int PROGRESS_BAR_MAX = 1000;
	private boolean condition = true;
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
		mOutputD = new File(mRootD, "outputs");
		if (!mOutputD.exists() || !mOutputD.isDirectory()) {
			mOutputD.mkdir();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_PICK: {
			final File file;
			if (resultCode == RESULT_OK) {
				if (condition) {
					try {
						File outputDir = new File(Environment.getExternalStorageDirectory(), "outputs");
						outputDir.mkdir();
						file = File.createTempFile("transcode_test", ".mp4", outputDir);
					} catch (IOException e) {
						Log.e(TAG, "Failed to create temporary file.", e);
						Toast.makeText(this, "Failed to create temporary file.", Toast.LENGTH_LONG).show();
						return;
					}
					ContentResolver resolver = getContentResolver();
					final ParcelFileDescriptor parcelFileDescriptor;
					try {
						parcelFileDescriptor = resolver.openFileDescriptor(data.getData(), "r");
					} catch (FileNotFoundException e) {
						Log.w("Could not open '" + data.getDataString() + "'", e);
						Toast.makeText(TranscoderActivity.this, "File not found.", Toast.LENGTH_LONG).show();
						return;
					}
					final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
					final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
					progressBar.setMax(PROGRESS_BAR_MAX);
					final long startTime = SystemClock.uptimeMillis();
					MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
						@Override
						public void onTranscodeProgress(double progress) {
							if (progress < 0) {
								progressBar.setIndeterminate(true);
							} else {
								progressBar.setIndeterminate(false);
								progressBar.setProgress((int) Math.round(progress * PROGRESS_BAR_MAX));
							}
						}

						@Override
						public void onTranscodeCompleted() {
							Log.d(TAG, "transcoding took " + (SystemClock.uptimeMillis() - startTime) + "ms");
							// onTranscodeFinished(true, "transcoded file placed on " + file, parcelFileDescriptor);
							// Uri uri = FileProvider.getUriForFile(TranscoderActivity.this, FILE_PROVIDER_AUTHORITY, file);
							// startActivity(new Intent(Intent.ACTION_VIEW)
							// .setDataAndType(uri, "video/mp4")
							// .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
						}

						@Override
						public void onTranscodeCanceled() {
							onTranscodeFinished(false, "Transcoder canceled.", parcelFileDescriptor);
						}

						@Override
						public void onTranscodeFailed(Exception exception) {
							exception.printStackTrace();
							onTranscodeFinished(false, "Transcoder error occurred.", parcelFileDescriptor);
						}
					};
					Log.d(TAG, "transcoding into " + file);
					CompressFileSizeFormatStrategy strategy = (CompressFileSizeFormatStrategy) MediaFormatStrategyPresets.createDecreaseBitrateFormatStrategy(600000, 128000, 2);
					strategy.setFrameRate(15);
					strategy.setIFrameInterval(5);
					mFuture = MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, file.getAbsolutePath(), strategy, listener);
					switchButtonEnabled(true);
				} else {
					Uri uri = data.getData();
					String inputPath = FileChooser.getAbsolutePathFromUri(this, uri);
					File inputF = new File(inputPath);
					ExtractDecodeEditEncodeMuxTest engine = new ExtractDecodeEditEncodeMuxTest();
					File outputF = new File(mOutputD, inputF.getName());
					if (outputF.exists()) {
						outputF.delete();
					}
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