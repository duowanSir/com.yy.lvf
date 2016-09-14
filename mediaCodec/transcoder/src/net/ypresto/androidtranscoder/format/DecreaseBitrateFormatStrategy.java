package net.ypresto.androidtranscoder.format;

import android.media.MediaFormat;

public class DecreaseBitrateFormatStrategy implements MediaFormatStrategy {
	

	@Override
	public MediaFormat createVideoOutputFormat(MediaFormat inputFormat) {
//		float bitrate = inputFormat.getInteger(MediaFormat.KEY_BIT_RATE);
		int targetBitrate = 600;
		MediaFormat targetMf = new MediaFormat();
		targetMf.setInteger(MediaFormat.KEY_BIT_RATE, targetBitrate);
		return targetMf;
	}

	@Override
	public MediaFormat createAudioOutputFormat(MediaFormat inputFormat) {
		return null;
	}

}
