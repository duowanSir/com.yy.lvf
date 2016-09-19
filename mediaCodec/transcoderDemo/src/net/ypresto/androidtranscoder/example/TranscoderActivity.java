package net.ypresto.androidtranscoder.example;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import net.ypresto.androidtranscoder.MediaTranscoder;
import net.ypresto.androidtranscoder.engine.InputSurface;
import net.ypresto.androidtranscoder.engine.OutputSurface;
import net.ypresto.androidtranscoder.format.MediaFormatExtraConstants;
import net.ypresto.androidtranscoder.format.MediaFormatStrategyPresets;

public class TranscoderActivity extends Activity {
	private static final String TAG = "TranscoderActivity";
	private static final String FILE_PROVIDER_AUTHORITY = "net.ypresto.androidtranscoder.example.fileprovider";
	private static final int REQUEST_CODE_PICK = 1;
	private static final int PROGRESS_BAR_MAX = 1000;
	private Future<Void> mFuture;

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

		File dcimF = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		File cameraF = new File(dcimF, "Camera");
		File video = null;
		if (cameraF.exists() && cameraF.isDirectory()) {
			File[] childrenF = cameraF.listFiles();
			for (File file : childrenF) {
				if (file.getName().endsWith(".mp4")) {
					video = file;
					break;
				}
			}
			final File tempVideoF = new File(cameraF, "temp.mp4");
			final File input = video;
			testTranscode(input, tempVideoF);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_PICK: {
			final File file;
			if (resultCode == RESULT_OK) {
				try {
					File outputDir = new File(getExternalFilesDir(null), "outputs");
					// noinspection ResultOfMethodCallIgnored
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
						onTranscodeFinished(true, "transcoded file placed on " + file, parcelFileDescriptor);
						Uri uri = FileProvider.getUriForFile(TranscoderActivity.this, FILE_PROVIDER_AUTHORITY, file);
						startActivity(new Intent(Intent.ACTION_VIEW)
							.setDataAndType(uri, "video/mp4")
							.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
					}

					@Override
					public void onTranscodeCanceled() {
						onTranscodeFinished(false, "Transcoder canceled.", parcelFileDescriptor);
					}

					@Override
					public void onTranscodeFailed(Exception exception) {
						onTranscodeFinished(false, "Transcoder error occurred.", parcelFileDescriptor);
					}
				};
				Log.d(TAG, "transcoding into " + file);
				// mFuture = MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, file.getAbsolutePath(),
				// MediaFormatStrategyPresets.createDecreaseBitrateFormatStrategy(), listener);
				mFuture = MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, file.getAbsolutePath(),
					MediaFormatStrategyPresets.createAndroid720pStrategy(8000 * 1000, 128 * 1000, 1), listener);
				switchButtonEnabled(true);
			}
			break;
		}
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
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

	public void testTranscode(File input, File output) {
		if (input == null || output == null) {
			throw new IllegalArgumentException("输入文件和输出文件不能为空");
		}

		MediaExtractor mediaExtractor = null;
		MediaMuxer mediaMuxer = null;
		FileInputStream fis = null;
		FileDescriptor fd = null;
		try {
			fis = new FileInputStream(input);
			fd = fis.getFD();
			mediaExtractor = new MediaExtractor();
			mediaExtractor.setDataSource(fd);
			mediaMuxer = new MediaMuxer(output.getAbsolutePath(),
				MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

			MediaMetadataRetriever mediaMetadataRetriever = null;
			String rotationStr = null;
			String dutationStr = null;
			String bitRateStr = null;
			long duration = 0;
			mediaMetadataRetriever = new MediaMetadataRetriever();
			mediaMetadataRetriever.setDataSource(fd);
			rotationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
			dutationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			bitRateStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
			duration = Long.valueOf(dutationStr);
			mediaMuxer.setOrientationHint(Integer.valueOf(rotationStr));
			Log.d(TAG, "rotationStr = " + rotationStr + " dutationStr = " + dutationStr + " bitRateStr = " + bitRateStr);
			if (duration <= 0) {
				throw new IllegalArgumentException("视频元数据异常，视频长度小于零");
			}

			int videoTrackIndex = -1;
			MediaFormat videoTrackFormat = null;
			String videoTrackMime = null;
			int audioTrackIndex = -1;
			MediaFormat audioTrackFormat = null;
			String audioTrackMime = null;
			int trackCount = mediaExtractor.getTrackCount();
			for (int i = 0; i < trackCount; i++) {
				MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
				String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
				if (videoTrackIndex < 0 && mime.startsWith("video/")) {
					videoTrackIndex = i;
					videoTrackMime = mime;
					videoTrackFormat = mediaFormat;
				} else if (audioTrackIndex < 0 && mime.startsWith("audio/")) {
					audioTrackIndex = i;
					audioTrackMime = mime;
					audioTrackFormat = mediaFormat;
				}
				if (videoTrackIndex >= 0 && audioTrackIndex >= 0) {
					break;
				}
			}
			if (videoTrackIndex < 0 || audioTrackIndex < 0) {
				throw new IllegalStateException("输入文件轨道错误");
			}

			// 创建视频输出格式
			int width = videoTrackFormat.getInteger(MediaFormat.KEY_WIDTH);
			int height = videoTrackFormat.getInteger(MediaFormat.KEY_HEIGHT);
			MediaFormat videoOutputFormat = MediaFormat.createVideoFormat("video/avc", width, height);
			videoOutputFormat.setInteger(MediaFormat.KEY_BIT_RATE, (int) (Long.valueOf(bitRateStr) / 2));
			videoOutputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 10);
			videoOutputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3);
			videoOutputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
			// 创建音频输出格式
			int keyRate = audioTrackFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
			int audioChannels = audioTrackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
			MediaFormat audioFormat = MediaFormat.createAudioFormat(MediaFormatExtraConstants.MIMETYPE_AUDIO_AAC, keyRate, audioChannels);
			audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
			audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128 * 1000);

			// 视频编码
			boolean mVideoEncoderStarted = false;
			MediaCodec videoEncoder = null;
			InputSurface videoInputSurface = null;
			ByteBuffer[] videoEncoderOutputBuffers = null;
			videoEncoder = MediaCodec.createEncoderByType(videoTrackMime);
			videoEncoder.configure(videoOutputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
			videoInputSurface = new InputSurface(videoEncoder.createInputSurface());
			videoInputSurface.makeCurrent();
			videoEncoder.start();
			mVideoEncoderStarted = true;
			videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();

			// 视频解码
			boolean videoDecodeStarted = false;
			MediaCodec videoDecoder = null;
			OutputSurface videoOutputSurface = null;
			ByteBuffer[] videoDecoderInputBuffers = null;
			if (videoTrackFormat.containsKey(MediaFormatExtraConstants.KEY_ROTATION_DEGREES)) {
				videoTrackFormat.setInteger(MediaFormatExtraConstants.KEY_ROTATION_DEGREES, 0);
			}
			videoOutputSurface = new OutputSurface();
			videoDecoder = MediaCodec.createDecoderByType(videoTrackFormat.getString(MediaFormat.KEY_MIME));
			videoDecoder.configure(videoTrackFormat, videoOutputSurface.getSurface(), null, 0);
			videoDecoder.start();
			videoDecodeStarted = true;
			videoDecoderInputBuffers = videoDecoder.getInputBuffers();

			mediaExtractor.selectTrack(videoTrackIndex);
			mediaExtractor.selectTrack(audioTrackIndex);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
