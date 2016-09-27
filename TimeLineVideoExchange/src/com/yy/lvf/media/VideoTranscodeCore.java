package com.yy.lvf.media;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaDataSource;
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

		while ((mNeedTranscodeAudio || mNeedTranscodeVideo) && !audioEncodeDone && !videoEncodeDone) {
			// 音频提取--->解码输入
			while (mNeedTranscodeAudio && !audioExtractDone && (audioEncoderDeterminedFormat == null || muxerStarted)) {
				int audioDecoderInputBufferIndex = mAudioDecoder.dequeueInputBuffer(TIMEOUT_US);
				if (audioDecoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE) {
						Log.d(TAG, "audio decoder: 输入阻塞");
					}
					break;
				}
				ByteBuffer input = audioDecoderInputBuffer[audioDecoderInputBufferIndex];
				int size = mAudioExtractor.readSampleData(input, 0);
				long timeUs = mAudioExtractor.getSampleTime();
				int flag = mAudioExtractor.getSampleFlags();
				if (VERBOSE) {
					Log.d(TAG, "audio extractor: "
						+ "[audio decoder input index: " + audioDecoderInputBufferIndex
						+ ", size: " + size
						+ ", presentation time: " + timeUs
						+ ",  flag: " + flag
						+ "]");
				}
				if (size > 0) {
					mAudioDecoder.queueInputBuffer(audioDecoderInputBufferIndex, 0, size, timeUs, flag);
				}
				if (size == -1) {
					mAudioDecoder.queueInputBuffer(audioDecoderInputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
					audioExtractDone = true;
					break;
				}
				audioExtractDone = !mAudioExtractor.advance();
				if (audioExtractDone) {
					mAudioDecoder.queueInputBuffer(audioDecoderInputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
					break;
				}
				break;
			}
			// 视频提取--->解码输入
			while (mNeedTranscodeVideo && !videoExtractDone && (videoEncoderDeterminedFormat == null || muxerStarted)) {
				int videoDecoderInputIndex = mVideoDecoder.dequeueInputBuffer(TIMEOUT_US);
				if (videoDecoderInputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE) {
						Log.d(TAG, "video extractor: 输入阻塞");
					}
					break;
				}
				ByteBuffer inputBuffer = videoDecoderInputBuffer[videoDecoderInputIndex];
				int size = mVideoExtractor.readSampleData(inputBuffer, 0);
				long timeUs = mVideoExtractor.getSampleTime();
				int flag = mVideoExtractor.getSampleFlags();
				if (VERBOSE) {
					Log.d(TAG, "video extractor: "
						+ "[video decoder input index: " + videoDecoderInputIndex
						+ ", size: " + size
						+ ", presentation time: " + timeUs
						+ ",  flag: " + flag
						+ "]");
				}
				if (size > 0) {
					mVideoDecoder.queueInputBuffer(videoDecoderInputIndex, 0, size, timeUs, flag);
				}
				if (size == -1) {
					mVideoDecoder.queueInputBuffer(videoDecoderInputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
					break;
				}
				videoExtractDone = !mVideoExtractor.advance();
				if (videoExtractDone) {
					mVideoDecoder.queueInputBuffer(videoDecoderInputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
					break;
				}
			}
			// 音频解码输出--->音频编码输入
			if (mNeedTranscodeAudio && !audioDecodeDone && audioPendingEncodeIndex == -1 && (audioEncoderDeterminedFormat == null || muxerStarted)) {
				int audioDecoderOutputIndex = mAudioDecoder.dequeueOutputBuffer(audioDecoderOutputBufferInfo, TIMEOUT_US);
				if (audioDecoderOutputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE) {
						Log.d(TAG, "audio decoder: 输入阻塞");
					}
				} else if (audioDecoderOutputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					audioDecoderOutputBuffer = mAudioDecoder.getOutputBuffers();
				} else if (audioDecoderOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				} else if (audioDecoderOutputIndex < 0) {
				} else {
					if ((audioDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
						mAudioDecoder.releaseOutputBuffer(audioDecoderOutputIndex, false);
					} else {
						audioPendingEncodeIndex = audioDecoderOutputIndex;
					}
				}
			}
			if (mNeedTranscodeAudio && !audioDecodeDone && audioPendingEncodeIndex != -1) {
				int inputIndex = mAudioEncoder.dequeueInputBuffer(TIMEOUT_US);
				if (inputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE) {
						Log.d(TAG, "audio encoder: 输入阻塞");
					}
				} else if (inputIndex >= 0) {
					ByteBuffer encoderInputBuffer = audioEncoderInputBuffer[inputIndex];
					encoderInputBuffer.position(0);
					ByteBuffer decoderOutputBuffer = audioDecoderOutputBuffer[audioPendingEncodeIndex].duplicate();
					decoderOutputBuffer.position(audioDecoderOutputBufferInfo.offset)
						.limit(audioDecoderOutputBufferInfo.offset + audioDecoderOutputBufferInfo.size);
					encoderInputBuffer.put(decoderOutputBuffer);

					mAudioEncoder.queueInputBuffer(audioPendingEncodeIndex, 0, audioDecoderOutputBufferInfo.size, audioDecoderOutputBufferInfo.presentationTimeUs, audioDecoderOutputBufferInfo.flags);
					mAudioDecoder.releaseOutputBuffer(audioPendingEncodeIndex, false);
					audioPendingEncodeIndex = -1;
					if ((audioDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
						audioDecodeDone = true;
					}
				}
			}
			// 视频解码输出--->视频编码输入
			if (mNeedTranscodeVideo && !videoDecodeDone && (videoEncoderDeterminedFormat == null || muxerStarted)) {
				int decoderOutputIndex = mVideoDecoder.dequeueOutputBuffer(videoDecoderOutputBufferInfo, TIMEOUT_US);
				if (decoderOutputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE) {
						Log.d(TAG, "video encoder: 输出阻塞");
					}
				} else if (decoderOutputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

				} else if (decoderOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

				} else if (decoderOutputIndex < 0) {

				} else {
					// BufferInfo的几个参数应该怎么判断和处理
					if ((videoDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
						mVideoDecoder.releaseOutputBuffer(decoderOutputIndex, false);
					} else {
						boolean render = (videoDecoderOutputBufferInfo.size > 0);
						if (render) {
							mVideoDecoderOutputSurface.awaitNewImage();
							mVideoDecoderOutputSurface.drawImage();
							mVideoEncoderInputSurface.setPresentationTime(videoDecoderOutputBufferInfo.presentationTimeUs * 1000);
							mVideoEncoderInputSurface.swapBuffers();
						}
						if ((videoDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
							videoDecodeDone = true;
							mVideoEncoder.signalEndOfInputStream();
						}
					}
				}
			}
			// 音频编码输出--->muxer输入
			if (mNeedTranscodeAudio && !audioEncodeDone && (audioEncoderDeterminedFormat == null || muxerStarted)) {
				int encoderOutputIndex = mVideoEncoder.dequeueOutputBuffer(audioEncoderOutputBufferInfo, TIMEOUT_US);
				if (encoderOutputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

				} else if (encoderOutputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					audioEncoderOutputBuffer = mAudioEncoder.getOutputBuffers();
				} else if (encoderOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					if (audioEncoderDeterminedFormat != null) {
						throw new RuntimeException("audio encoder: output format change twice");
					}
					audioEncoderDeterminedFormat = mAudioEncoder.getOutputFormat();
					if (!muxerStarted) {
						mMuxer.addTrack(audioEncoderDeterminedFormat);
						mMuxer.start();
						muxerStarted = true;
					}
				} else if (encoderOutputIndex < 0) {

				} else {
					if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
						mAudioEncoder.releaseOutputBuffer(encoderOutputIndex, false);
					} else {
						ByteBuffer encoderOutputBuffer = audioEncoderOutputBuffer[encoderOutputIndex];
						encoderOutputBuffer.position(audioEncoderOutputBufferInfo.offset)
							.limit(audioEncoderOutputBufferInfo.offset + audioEncoderOutputBufferInfo.size);
						mMuxer.writeSampleData(mInputAudioTrack, encoderOutputBuffer, audioEncoderOutputBufferInfo);
						mAudioEncoder.releaseOutputBuffer(encoderOutputIndex, false);
						if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
							audioEncodeDone = true;
						}
					}
				}
			}
			// 视频编码输出--->muxer输入
			if (mNeedTranscodeVideo && !videoEncodeDone && (videoEncoderDeterminedFormat == null || muxerStarted)) {
				int encoderOutputIndex = mVideoEncoder.dequeueOutputBuffer(videoEncoderOutputBufferInfo, TIMEOUT_US);
				if (encoderOutputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

				} else if (encoderOutputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					videoEncoderOutputBuffer = mVideoEncoder.getOutputBuffers();
				} else if (encoderOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					if (videoEncoderDeterminedFormat != null) {
						throw new RuntimeException("video encoder: output format change twice");
					}
					videoEncoderDeterminedFormat = mVideoEncoder.getOutputFormat();
					if (!muxerStarted) {
						mMuxer.addTrack(videoEncoderDeterminedFormat);
						mMuxer.start();
						muxerStarted = true;
					}
				} else if (encoderOutputIndex < 0) {

				} else {
					if ((videoEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
						mVideoEncoder.releaseOutputBuffer(encoderOutputIndex, false);
					} else {
						ByteBuffer encoderOutputBuffer = videoEncoderOutputBuffer[encoderOutputIndex];
						encoderOutputBuffer.position(videoEncoderOutputBufferInfo.offset)
							.limit(videoEncoderOutputBufferInfo.offset + videoEncoderOutputBufferInfo.size);
						mMuxer.writeSampleData(mInputVideoTrack, encoderOutputBuffer, videoEncoderOutputBufferInfo);
						mVideoEncoder.releaseOutputBuffer(encoderOutputIndex, false);
						if ((videoEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
							videoEncodeDone = true;
						}
					}
				}
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
