package com.yy.lvf.media;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaMuxer.OutputFormat;
import android.util.Log;
import android.view.Surface;

public class VideoTranscodeCore {
	public static final boolean VERBOSE = false;
	public static final String TAG = VideoTranscodeCore.class.getSimpleName();
	public static final int TIMEOUT_US = 10000;
	
	private File mInputF;
	private File mOutputF;

	private boolean mNeedTranscodeVideo;
	private boolean mNeedTranscodeAudio;

	private String mOutputVideoMime;
	private int mOutputVideoWidth;
	private int mOutputVideoHeight;
	private int mOutputVideoBitRate;
	private int mOutputVideoFrameRate;
	private int mOutputVideoIFrameInterval;

	private String mOutputAudioMime;
	private int mOutputAudioBitRate;
	private int mOutputAudioChannelCount;

	private MediaExtractor mAudioExtractor;
	private MediaExtractor mVideoExtractor;
	private MediaCodec mAudioDecoder;
	private MediaCodec mVideoDecoder;
	private OutputSurface mVideoDecoderOutputSurface;
	private MediaCodec mAudioEncoder;
	private MediaCodec mVideoEncoder;
	private InputSurface mVideoEncoderInputSurface;
	private MediaMuxer mMuxer;
	private ByteBuffer mMuxerInputBuffer;

	private int mInputVideoTrack;
	private int mInputAudioTrack;

	private VideoTranscodeCore() {
	}

	private boolean init() {
		if (mNeedTranscodeAudio) {
			MediaCodecInfo encoderInfo = checkCapabilities(mOutputAudioMime, true);
			if (encoderInfo == null) {
				if (VERBOSE) {
					Log.d(TAG, "codec info: 不支持" + mOutputAudioMime + "的编码");
				}
				return false;
			}
			mAudioExtractor = new MediaExtractor();
			try {
				mAudioExtractor.setDataSource(mInputF.getAbsolutePath());
				mInputAudioTrack = getTrackByMime(mAudioExtractor, mOutputAudioMime);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (mInputAudioTrack == -1) {
				if (VERBOSE) {
					Log.d(TAG, "extractor: 未找到audio mime对应的track");
				}
				return false;
			}
			String inputAudioTrackMime = mAudioExtractor.getTrackFormat(mInputAudioTrack).getString(MediaFormat.KEY_MIME);
			MediaCodecInfo decoderInfo = checkCapabilities(inputAudioTrackMime, false);
			if (decoderInfo == null) {
				if (VERBOSE) {
					Log.d(TAG, "codec info: 不支持" + inputAudioTrackMime + "的解码");
				}
				return false;
			}
			try {
				mAudioDecoder = MediaCodec.createByCodecName(encoderInfo.getName());
				mAudioDecoder.configure(mAudioExtractor.getTrackFormat(mInputAudioTrack), null, null, 0);
				mAudioDecoder.start();

				MediaFormat outputAudioTrackFormat = createAudioTrackFormat(mAudioExtractor.getTrackFormat(mInputAudioTrack));
				mAudioEncoder = MediaCodec.createByCodecName(encoderInfo.getName());
				mAudioEncoder.configure(outputAudioTrackFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
				mAudioEncoder.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (mNeedTranscodeVideo) {
			MediaCodecInfo encoderInfo = checkCapabilities(mOutputVideoMime, true);
			if (encoderInfo == null) {
				if (VERBOSE) {
					Log.d(TAG, "codec info: 不支持" + mOutputVideoMime + "的编码");
				}
				return false;
			}
			mVideoExtractor = new MediaExtractor();
			try {
				mVideoExtractor.setDataSource(mInputF.getAbsolutePath());
				mInputVideoTrack = getTrackByMime(mVideoExtractor, "video");
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (mInputVideoTrack == -1) {
				if (VERBOSE) {
					Log.d(TAG, "extractor: 未找到video track");
				}
				return false;
			}
			String inputVideoTrackMime = mVideoExtractor.getTrackFormat(mInputVideoTrack).getString(MediaFormat.KEY_MIME);
			MediaCodecInfo decoderInfo = checkCapabilities(inputVideoTrackMime, false);
			if (decoderInfo == null) {
				if (VERBOSE) {
					Log.d(TAG, "codec info: 不支持" + inputVideoTrackMime + "的解码");
				}
				return false;
			}
			try {
				mVideoDecoder = MediaCodec.createByCodecName(decoderInfo.getName());
				mVideoDecoderOutputSurface = new OutputSurface();
				mVideoDecoder.configure(mVideoExtractor.getTrackFormat(mInputVideoTrack), mVideoDecoderOutputSurface.getSurface(), null, 0);
				mVideoDecoder.start();

				MediaFormat outputVideoTrackFormat = createVideoTrackFormat(mVideoExtractor.getTrackFormat(mInputVideoTrack));
				AtomicReference<Surface> ar = new AtomicReference<Surface>();
				mVideoEncoder = MediaCodec.createByCodecName(encoderInfo.getName());
				mVideoEncoder.configure(outputVideoTrackFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
				ar.set(mVideoEncoder.createInputSurface());
				mVideoEncoderInputSurface = new InputSurface(ar.get());
				mVideoEncoder.start();
				mVideoEncoderInputSurface.makeCurrent();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		try {
			mMuxer = new MediaMuxer(mOutputF.getAbsolutePath(), OutputFormat.MUXER_OUTPUT_MPEG_4);
			mMuxerInputBuffer = ByteBuffer.allocate(5120);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mNeedTranscodeAudio || mNeedTranscodeVideo;
	}

	public void syncDoExtractDecodeEncodeMux() {
		boolean audioExtractDone = false;
		boolean videoExtractDone = false;

		boolean audioDecodeDone = false;
		int audioPendingEncodeIndex = 0;
		boolean videoDecodeDone = false;

		boolean audioEncodeDone = false;
		MediaFormat audioEncoderDeterminedFormat = null;
		boolean videoEncodeDone = false;
		MediaFormat videoEncoderDeterminedFormat = null;

		boolean muxerStarted = false;
		boolean muxerDone = false;

		ByteBuffer[] audioDecoderInputBuffer = mAudioDecoder.getInputBuffers();
		ByteBuffer[] videoDecoderInputBuffer = mVideoDecoder.getInputBuffers();
		
		ByteBuffer[] audioDecoderOutputBuffer = mAudioDecoder.getOutputBuffers();
		MediaCodec.BufferInfo audioDecoderOutputBufferInfo = new MediaCodec.BufferInfo();
		// video decoder使用surface render输出，所以没有对应的输出buffer。
		MediaCodec.BufferInfo videoDecoderOutputBufferInfo = new MediaCodec.BufferInfo();
		
		ByteBuffer[] audioEncoderInputBuffer = mAudioEncoder.getInputBuffers();
		// video encoder使用surface和video decoder的输出surface交换数据，所以没有对应的输入buffer。
		
		ByteBuffer[] audioEncoderOutputBuffer = mAudioEncoder.getOutputBuffers();
		MediaCodec.BufferInfo audioEncoderOutputBufferInfo = new MediaCodec.BufferInfo();
		ByteBuffer[] videoEncoderOutputBuffer = mVideoEncoder.getOutputBuffers();
		MediaCodec.BufferInfo videoEncoderOutputBufferInfo = new MediaCodec.BufferInfo();
		
		while ((mNeedTranscodeAudio || mNeedTranscodeVideo) && !muxerDone) {
			while (mNeedTranscodeAudio && !audioExtractDone && (audioEncoderDeterminedFormat == null || muxerStarted)) {
				int audioDecoderInputBufferIndex = mAudioDecoder.dequeueInputBuffer(TIMEOUT_US);
				if (audioDecoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE) {
						Log.d(TAG, "audio decoder: 输入阻塞" );
					}
					break;
				}
				ByteBuffer input = audioDecoderInputBuffer[audioDecoderInputBufferIndex];
				int size = mAudioExtractor.readSampleData(input, 0);
				
			}
		}
	}

	private MediaFormat createVideoTrackFormat(MediaFormat inputFormat) {
		MediaFormat outputVideoTrackFormat = MediaFormat.createVideoFormat(mOutputVideoMime, mOutputVideoWidth, mOutputVideoHeight);
		outputVideoTrackFormat.setInteger(MediaFormat.KEY_BIT_RATE, mOutputVideoBitRate);
		outputVideoTrackFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mOutputVideoFrameRate);
		outputVideoTrackFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mOutputVideoIFrameInterval);
		outputVideoTrackFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
		return outputVideoTrackFormat;
	}

	private MediaFormat createAudioTrackFormat(MediaFormat inputFormat) {
		MediaFormat outputAudioTrackFormat = MediaFormat.createAudioFormat(mOutputAudioMime, inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE), mOutputAudioChannelCount);
		outputAudioTrackFormat.setInteger(MediaFormat.KEY_BIT_RATE, mOutputAudioBitRate);
		return outputAudioTrackFormat;
	}

	public static int getTrackByMime(MediaExtractor extractor, String mime) {
		int track = -1;
		int trackCount = extractor.getTrackCount();
		for (int i = 0; i < trackCount; i++) {
			MediaFormat format = extractor.getTrackFormat(i);
			if (format != null && format.containsKey(MediaFormat.KEY_MIME) && format.getString(MediaFormat.KEY_MIME).contains(mime)) {
				track = i;
				break;
			}
			continue;
		}
		return track;
	}

	public MediaCodecInfo checkCapabilities(String mime, boolean isEncoder) {
		for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
			String[] types = codecInfo.getSupportedTypes();
			if (types != null) {
				for (String supportedMime : types) {
					if (supportedMime != null && supportedMime.equalsIgnoreCase(mime)) {
						if (isEncoder == codecInfo.isEncoder()) {
							return codecInfo;
						}
					}
				}
			}
		}
		return null;
	}

}
