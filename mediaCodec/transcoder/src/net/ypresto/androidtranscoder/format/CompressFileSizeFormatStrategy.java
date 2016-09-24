package net.ypresto.androidtranscoder.format;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

public class CompressFileSizeFormatStrategy implements MediaFormatStrategy {
	public static final boolean VERBOSE = true;
	public static double GOLDEN_RATIO = 0.618;
	private int mOutputVideoBitrate;
	private int mOutputVideoFrameRate;
	private int mOutputVideoIFrameInterval;

	private int mOutputAudioBitRate;
	private int mOutputAudioChannelCount;

	public CompressFileSizeFormatStrategy(int videoBitRate, int audioBitRate, int audioChannels) {
		mOutputVideoBitrate = videoBitRate;
		mOutputAudioBitRate = audioBitRate;
		mOutputAudioChannelCount = audioChannels;
	}

	@Override
	public MediaFormat createVideoOutputFormat(MediaFormat inputFormat) {
		int width = inputFormat.getInteger(MediaFormat.KEY_WIDTH);
		int height = inputFormat.getInteger(MediaFormat.KEY_HEIGHT);
		MediaFormat outputVideoFormat = MediaFormat.createVideoFormat("video/avc", (int) (width * GOLDEN_RATIO), (int) (height * GOLDEN_RATIO));
		outputVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
		outputVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, mOutputVideoBitrate);
		outputVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mOutputVideoFrameRate);
		outputVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mOutputVideoIFrameInterval);
		if (VERBOSE) {
			Log.d("CompressFileSizeFormatStrategy", "outputVideoFormat: " + outputVideoFormat);
		}
		return outputVideoFormat;
	}

	@Override
	public MediaFormat createAudioOutputFormat(MediaFormat inputFormat) {
		MediaFormat outputAudioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE), mOutputAudioChannelCount);
		outputAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, mOutputAudioBitRate);
		if (VERBOSE) {
			Log.d("CompressFileSizeFormatStrategy", "outputAudioFormat: " + outputAudioFormat);
		}
		return outputAudioFormat;
	}

	public void setFrameRate(int frameRate) {
		mOutputVideoFrameRate = frameRate;
	}

	public void setIFrameInterval(int iFrameInterval) {
		mOutputVideoIFrameInterval = iFrameInterval;
	}

}
