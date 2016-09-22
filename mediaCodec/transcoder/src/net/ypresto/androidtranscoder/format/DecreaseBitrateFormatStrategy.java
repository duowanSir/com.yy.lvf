package net.ypresto.androidtranscoder.format;

import java.io.FileDescriptor;

import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.text.TextUtils;

public class DecreaseBitrateFormatStrategy implements MediaFormatStrategy {
	public static final long MAX_SIZE = 8388608; // 1M文件bit数

	private long mDurationUs;
	private int mWidth;
	private int mHeight;
	private String mMime;
	private int mOutputVideoBitrate;

	public DecreaseBitrateFormatStrategy(FileDescriptor input, String mime) {
		mMime = mime;
		MediaExtractor extractor;
		try {
			extractor = new MediaExtractor();
			extractor.setDataSource(input);
			for (int i = 0; i < extractor.getTrackCount(); i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				if (!format.getString(MediaFormat.KEY_MIME).contains("video")) {
					continue;
				}
				mDurationUs = format.getLong(MediaFormat.KEY_DURATION);
				mWidth = format.getInteger(MediaFormat.KEY_WIDTH);
				mHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
				break;
			}
			mOutputVideoBitrate = (int) (8388608 / ((float) (mDurationUs / 1000000)) / 1024);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public MediaFormat createVideoOutputFormat(MediaFormat inputFormat) {
		if (TextUtils.isEmpty(mMime) || mWidth <= 0 || mHeight <= 0) {
			return null;
		}
		MediaFormat outputVideoFormat = MediaFormat.createVideoFormat(mMime, mWidth, mHeight);
		outputVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
		outputVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, mOutputVideoBitrate);
		outputVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 5);
		outputVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
		return outputVideoFormat;
	}

	@Override
	public MediaFormat createAudioOutputFormat(MediaFormat inputFormat) {
		return null;
	}

}
