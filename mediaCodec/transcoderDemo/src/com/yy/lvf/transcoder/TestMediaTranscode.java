package com.yy.lvf.transcoder;
//package net.ypresto.androidtranscoder.example;
//
//import java.io.File;
//import java.io.FileDescriptor;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.concurrent.atomic.AtomicReference;
//
//import net.ypresto.androidtranscoder.engine.InputSurface;
//import net.ypresto.androidtranscoder.engine.OutputSurface;
//import android.media.MediaCodec;
//import android.media.MediaCodecInfo;
//import android.media.MediaCodecList;
//import android.media.MediaExtractor;
//import android.media.MediaFormat;
//import android.media.MediaMuxer;
//import android.util.Log;
//import android.view.Surface;
//
//public class TestMediaTranscode {
//	public final String TAG = TestMediaTranscode.class.getSimpleName();
//	private static final String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n"
//		+ "precision mediump float;\n" + "varying vec2 vTextureCoord;\n"
//		+ "uniform samplerExternalOES sTexture;\n"
//		+ "void main() {\n"
//		+ "  gl_FragColor = texture2D(sTexture, vTextureCoord).rbga;\n"
//		+ "}\n";
//	private static final String OUTPUT_AUDIO_MIME_TYPE = "audio/mp4a-latm"; // Advanced Audio Coding
//	private static final int OUTPUT_AUDIO_CHANNEL_COUNT = 2; // Must match the input stream.
//	private static final int OUTPUT_AUDIO_BIT_RATE = 128 * 1024;
//	private static final int OUTPUT_AUDIO_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectHE;
//	private static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 44100; // Must match the input stream.
//	private static final int TIMEOUT_USEC = 10000;
//	private static boolean VERBOSE = true;
//
//	public int mWidth = 0;
//	public int mHeight = 0;
//	public File mInputVideo = null;
//	public File mOutputVideo = null;
//	public String mOutputVideoMime = null;
//	public String mOutputAudioMime = null;
//	public int mOutputVideoBitRate = 0;
//	public int mOutputVideoFrameRate = 0;
//	public int mOutputVideoIFrameInterval = 0;
//
//	public boolean mNeedCopyAudio = false;
//	public boolean mNeedCopyVideo = false;
//	public boolean mNeedVerifyAudio = false;
//
//	public static class BaseTrackInfo {
//		public MediaFormat mTrackFormat;
//		public int mTrackIndex;
//		public String mTrackMime;
//	}
//
//	public void transcode() throws Exception {
//		Exception exception = null;
//
//		MediaCodecInfo videoCodecInfo = selectCodec(mOutputVideoMime);
//		if (videoCodecInfo == null) {
//			Log.d(TAG, "目标视频mime不支持:" + mOutputVideoMime);
//			return;
//		}
//		MediaCodecInfo audioCodecInfo = selectCodec(mOutputAudioMime);
//		if (audioCodecInfo == null) {
//			Log.d(TAG, "目标音频mime不支持:" + mOutputVideoMime);
//			return;
//		}
//
//		MediaExtractor videoExtractor = null;
//		MediaExtractor audioExtractor = null;
//		MediaCodec videoDecoder = null;
//		MediaCodec audioDecoder = null;
//		MediaCodec videoEncoder = null;
//		MediaCodec audioEncoder = null;
//		MediaMuxer muxer = null;
//		OutputSurface outputSurface = null;
//		InputSurface inputSurface = null;
//
//		BaseTrackInfo inputVideoBaseTrackInfo = null;
//		BaseTrackInfo inputAudioBaseTrackInfo = null;
//
//		MediaFormat outputVideoFormat = null;
//
//		if (mNeedCopyVideo) {
//			videoExtractor = createExtractor(mInputVideo);
//			if (videoExtractor == null) {
//				Log.d(TAG, "创建extractor失败");
//				return;
//			}
//			inputVideoBaseTrackInfo = getBaseTrackInfo(videoExtractor, "video");
//			outputVideoFormat = MediaFormat.createVideoFormat(mOutputVideoMime, mWidth, mHeight);
//			// 必备属性，设置这些属性失败的时候，会导致调用MediaCodec.config()抛异常。
//			outputVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//			outputVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, mOutputVideoBitRate);
//			outputVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mOutputVideoFrameRate);
//			outputVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mOutputVideoIFrameInterval);
//			// 为期望的视频编码创建编码Codec，并请求一个Surface作为输入。
//			AtomicReference<Surface> inputSurfaceReference = new AtomicReference<Surface>();
//			videoEncoder = createVideoEncoder(videoCodecInfo, outputVideoFormat, inputSurfaceReference);
//			inputSurface = new InputSurface(inputSurfaceReference.get());
//			inputSurface.makeCurrent();
//			// 创建解码器
//			outputSurface = new OutputSurface();
//			outputSurface.changeFragmentShader(FRAGMENT_SHADER);
//			videoDecoder = createVideoDecoder(inputVideoBaseTrackInfo.mTrackFormat, outputSurface.getSurface());
//		}
//
//		if (mNeedCopyAudio) {
//			audioExtractor = createExtractor(mInputVideo);
//			if (videoExtractor == null) {
//				Log.d(TAG, "创建extractor失败");
//				return;
//			}
//			inputAudioBaseTrackInfo = getBaseTrackInfo(videoExtractor, "audio");
//			MediaFormat outputAudioFormat = MediaFormat.createAudioFormat(OUTPUT_AUDIO_MIME_TYPE, OUTPUT_AUDIO_SAMPLE_RATE_HZ, OUTPUT_AUDIO_CHANNEL_COUNT);
//			outputAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, OUTPUT_AUDIO_BIT_RATE);
//			outputAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, OUTPUT_AUDIO_AAC_PROFILE);
//
//			audioEncoder = createAudioEncoder(audioCodecInfo, outputAudioFormat);
//			audioDecoder = createAudioDecoder(inputAudioBaseTrackInfo.mTrackFormat);
//		}
//
//		muxer = new MediaMuxer(mOutputVideo.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//	}
//
//	private static MediaCodecInfo selectCodec(String mimeType) {
//		int numCodecs = MediaCodecList.getCodecCount();
//		for (int i = 0; i < numCodecs; i++) {
//			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
//			if (!codecInfo.isEncoder()) {
//				continue;
//			}
//			String[] types = codecInfo.getSupportedTypes();
//			for (int j = 0; j < types.length; j++) {
//				if (types[j].equalsIgnoreCase(mimeType)) {
//					return codecInfo;
//				}
//			}
//		}
//		return null;
//	}
//
//	public static MediaExtractor createExtractor(File f) {
//		if (f == null || !f.exists() || f.isDirectory()) {
//			return null;
//		}
//		FileInputStream is = null;
//		FileDescriptor fd = null;
//		MediaExtractor extractor = null;
//		try {
//			is = new FileInputStream(f);
//			fd = is.getFD();
//			extractor = new MediaExtractor();
//			extractor.setDataSource(fd);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			if (is != null) {
//				try {
//					is.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				is = null;
//			}
//		}
//		return extractor;
//	}
//
//	public static BaseTrackInfo getBaseTrackInfo(MediaExtractor extractor, String mime) {
//		int trackCount = 0;
//		BaseTrackInfo info = null;
//		trackCount = extractor.getTrackCount();
//
//		for (int i = 0; i < trackCount; i++) {
//			MediaFormat format = extractor.getTrackFormat(i);
//			String mimeStr = format.getString(MediaFormat.KEY_MIME);
//			if (mimeStr.contains(mime)) {
//				info = new BaseTrackInfo();
//				info.mTrackFormat = format;
//				info.mTrackIndex = i;
//				info.mTrackMime = mimeStr;
//				break;
//			}
//		}
//		return info;
//	}
//
//	private MediaCodec createVideoEncoder(MediaCodecInfo codecInfo, MediaFormat format, AtomicReference<Surface> surfaceReference) throws IOException {
//		MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
//		encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//		surfaceReference.set(encoder.createInputSurface());// 必须在start之前调用
//		encoder.start();
//		return encoder;
//	}
//
//	private MediaCodec createVideoDecoder(MediaFormat inputFormat, Surface surface) throws IOException {
//		MediaCodec decoder = MediaCodec.createDecoderByType(inputFormat.getString(MediaFormat.KEY_MIME));
//		decoder.configure(inputFormat, surface, null, 0);
//		decoder.start();
//		return decoder;
//	}
//
//	private MediaCodec createAudioEncoder(MediaCodecInfo codecInfo, MediaFormat format) throws IOException {
//		MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
//		encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//		encoder.start();
//		return encoder;
//	}
//
//	private MediaCodec createAudioDecoder(MediaFormat inputFormat) throws IOException {
//		MediaCodec decoder = MediaCodec.createDecoderByType(inputFormat.getString(MediaFormat.KEY_MIME));
//		decoder.configure(inputFormat, null, null, 0);
//		decoder.start();
//		return decoder;
//	}
//
//	private void doExtractDecodeEditEncodeMux(MediaExtractor videoExtractor, MediaExtractor audioExtractor, MediaCodec videoDecoder, MediaCodec videoEncoder, MediaCodec audioDecoder, MediaCodec audioEncoder, MediaMuxer muxer, InputSurface inputSurface, OutputSurface outputSurface) {
//		ByteBuffer[] videoDecoderInputBuffers = null;
//		ByteBuffer[] videoDecoderOutputBuffers = null;
//		ByteBuffer[] videoEncoderOutputBuffers = null;
//		MediaCodec.BufferInfo videoDecoderOutputBufferInfo = null;
//		MediaCodec.BufferInfo videoEncoderOutputBufferInfo = null;
//		if (mNeedCopyVideo) {
//			videoDecoderInputBuffers = videoDecoder.getInputBuffers();
//			videoDecoderOutputBuffers = videoDecoder.getOutputBuffers();
//			videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
//			videoDecoderOutputBufferInfo = new MediaCodec.BufferInfo();
//			videoEncoderOutputBufferInfo = new MediaCodec.BufferInfo();
//		}
//		ByteBuffer[] audioDecoderInputBuffers = null;
//		ByteBuffer[] audioDecoderOutputBuffers = null;
//		ByteBuffer[] audioEncoderInputBuffers = null;
//		ByteBuffer[] audioEncoderOutputBuffers = null;
//		MediaCodec.BufferInfo audioDecoderOutputBufferInfo = null;
//		MediaCodec.BufferInfo audioEncoderOutputBufferInfo = null;
//		if (mNeedCopyAudio) {
//			audioDecoderInputBuffers = audioDecoder.getInputBuffers();
//			audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
//			audioEncoderInputBuffers = audioEncoder.getInputBuffers();
//			audioEncoderOutputBuffers = audioEncoder.getOutputBuffers();
//			audioDecoderOutputBufferInfo = new MediaCodec.BufferInfo();
//			audioEncoderOutputBufferInfo = new MediaCodec.BufferInfo();
//		}
//		// We will get these from the decoders when notified of a format change.
//		MediaFormat decoderOutputVideoFormat = null;
//		MediaFormat decoderOutputAudioFormat = null;
//		// We will get these from the encoders when notified of a format change.
//		MediaFormat encoderOutputVideoFormat = null;
//		MediaFormat encoderOutputAudioFormat = null;
//		// We will determine these once we have the output format.
//		int outputVideoTrack = -1;
//		int outputAudioTrack = -1;
//		// Whether things are done on the video side.
//		boolean videoExtractorDone = false;
//		boolean videoDecoderDone = false;
//		boolean videoEncoderDone = false;
//		// Whether things are done on the audio side.
//		boolean audioExtractorDone = false;
//		boolean audioDecoderDone = false;
//		boolean audioEncoderDone = false;
//		// The audio decoder output buffer to process, -1 if none.
//		int pendingAudioDecoderOutputBufferIndex = -1;
//
//		boolean muxing = false;
//
//		int videoExtractedFrameCount = 0;
//		int videoDecodedFrameCount = 0;
//		int videoEncodedFrameCount = 0;
//
//		int audioExtractedFrameCount = 0;
//		int audioDecodedFrameCount = 0;
//		int audioEncodedFrameCount = 0;
//
//		while ((mNeedCopyVideo && !videoEncoderDone) || (mNeedCopyAudio && !audioEncoderDone)) {
//			if (VERBOSE) {
//				Log.d(TAG, String.format("loop: "
//
//					+ "V(%b){" + "extracted:%d(done:%b) " + "decoded:%d(done:%b) " + "encoded:%d(done:%b)} "
//
//					+ "A(%b){" + "extracted:%d(done:%b) " + "decoded:%d(done:%b) " + "encoded:%d(done:%b) " + "pending:%d} "
//
//					+ "muxing:%b(V:%d,A:%d)",
//
//					mNeedCopyVideo, videoExtractedFrameCount, videoExtractorDone, videoDecodedFrameCount, videoDecoderDone, videoEncodedFrameCount, videoEncoderDone,
//
//					mNeedCopyAudio, audioExtractedFrameCount, audioExtractorDone, audioDecodedFrameCount, audioDecoderDone, audioEncodedFrameCount, audioEncoderDone, pendingAudioDecoderOutputBufferIndex,
//
//					muxing, outputVideoTrack, outputAudioTrack));
//			}
//
//			// Extract video from file and feed to decoder.
//			// Do not extract video if we have determined the output format but
//			// we are not yet
//			// ready to mux the frames.
//			while (mNeedCopyVideo && !videoExtractorDone && (encoderOutputVideoFormat == null || muxing)) {
//				int decoderInputBufferIndex = videoDecoder.dequeueInputBuffer(TIMEOUT_USEC);
//				if (decoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
//					if (VERBOSE)
//						Log.d(TAG, "no video decoder input buffer");
//					break;
//				}
//				if (VERBOSE) {
//					Log.d(TAG, "video decoder: returned input buffer: " + decoderInputBufferIndex);
//				}
//				ByteBuffer decoderInputBuffer = videoDecoderInputBuffers[decoderInputBufferIndex];
//				int size = videoExtractor.readSampleData(decoderInputBuffer, 0);
//				long presentationTime = videoExtractor.getSampleTime();
//				if (VERBOSE) {
//					Log.d(TAG, "video extractor: returned buffer of size " + size);
//					Log.d(TAG, "video extractor: returned buffer for time " + presentationTime);
//				}
//				if (size >= 0) {
//					videoDecoder.queueInputBuffer(decoderInputBufferIndex, 0, size, presentationTime, videoExtractor.getSampleFlags());
//				}
//				videoExtractorDone = !videoExtractor.advance();
//				if (videoExtractorDone) {
//					if (VERBOSE)
//						Log.d(TAG, "video extractor: EOS");
//					videoDecoder.queueInputBuffer(decoderInputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//				}
//				videoExtractedFrameCount++;
//				// We extracted a frame, let's try something else next.
//				break;
//			}
//
//			// Extract audio from file and feed to decoder.
//			// Do not extract audio if we have determined the output format but
//			// we are not yet
//			// ready to mux the frames.
//			while (mNeedCopyAudio && !audioExtractorDone && (encoderOutputAudioFormat == null || muxing)) {
//				int decoderInputBufferIndex = audioDecoder.dequeueInputBuffer(TIMEOUT_USEC);
//				if (decoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
//					if (VERBOSE)
//						Log.d(TAG, "no audio decoder input buffer");
//					break;
//				}
//				if (VERBOSE) {
//					Log.d(TAG, "audio decoder: returned input buffer: " + decoderInputBufferIndex);
//				}
//				ByteBuffer decoderInputBuffer = audioDecoderInputBuffers[decoderInputBufferIndex];
//				int size = audioExtractor.readSampleData(decoderInputBuffer, 0);
//				long presentationTime = audioExtractor.getSampleTime();
//				if (VERBOSE) {
//					Log.d(TAG, "audio extractor: returned buffer of size " + size);
//					Log.d(TAG, "audio extractor: returned buffer for time " + presentationTime);
//				}
//				if (size >= 0) {
//					audioDecoder.queueInputBuffer(decoderInputBufferIndex, 0, size, presentationTime, audioExtractor.getSampleFlags());
//				}
//				audioExtractorDone = !audioExtractor.advance();
//				if (audioExtractorDone) {
//					if (VERBOSE)
//						Log.d(TAG, "audio extractor: EOS");
//					audioDecoder.queueInputBuffer(decoderInputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//				}
//				audioExtractedFrameCount++;
//				// We extracted a frame, let's try something else next.
//				break;
//			}
//
//			// Poll output frames from the video decoder and feed the encoder.
//			while (mNeedCopyVideo && !videoDecoderDone && (encoderOutputVideoFormat == null || muxing)) {
//				int decoderOutputBufferIndex = videoDecoder.dequeueOutputBuffer(videoDecoderOutputBufferInfo, TIMEOUT_USEC);
//				if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
//					if (VERBOSE)
//						Log.d(TAG, "no video decoder output buffer");
//					break;
//				}
//				if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//					if (VERBOSE)
//						Log.d(TAG, "video decoder: output buffers changed");
//					videoDecoderOutputBuffers = videoDecoder.getOutputBuffers();
//					break;
//				}
//				if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//					decoderOutputVideoFormat = videoDecoder.getOutputFormat();
//					if (VERBOSE) {
//						Log.d(TAG, "video decoder: output format changed: " + decoderOutputVideoFormat);
//					}
//					break;
//				}
//				if (VERBOSE) {
//					Log.d(TAG, "video decoder: returned output buffer: " + decoderOutputBufferIndex);
//					Log.d(TAG, "video decoder: returned buffer of size " + videoDecoderOutputBufferInfo.size);
//				}
//				ByteBuffer decoderOutputBuffer = videoDecoderOutputBuffers[decoderOutputBufferIndex];
//				if ((videoDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//					if (VERBOSE)
//						Log.d(TAG, "video decoder: codec config buffer");
//					videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);
//					break;
//				}
//				if (VERBOSE) {
//					Log.d(TAG, "video decoder: returned buffer for time " + videoDecoderOutputBufferInfo.presentationTimeUs);
//				}
//				boolean render = videoDecoderOutputBufferInfo.size != 0;
//				videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex, render);
//				if (render) {
//					if (VERBOSE)
//						Log.d(TAG, "output surface: await new image");
//					outputSurface.awaitNewImage();
//					// Edit the frame and send it to the encoder.
//					if (VERBOSE)
//						Log.d(TAG, "output surface: draw image");
//					outputSurface.drawImage();
//					inputSurface.setPresentationTime(videoDecoderOutputBufferInfo.presentationTimeUs * 1000);
//					if (VERBOSE)
//						Log.d(TAG, "input surface: swap buffers");
//					inputSurface.swapBuffers();
//					if (VERBOSE)
//						Log.d(TAG, "video encoder: notified of new frame");
//				}
//				if ((videoDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//					if (VERBOSE)
//						Log.d(TAG, "video decoder: EOS");
//					videoDecoderDone = true;
//					videoEncoder.signalEndOfInputStream();
//				}
//				videoDecodedFrameCount++;
//				// We extracted a pending frame, let's try something else next.
//				break;
//			}
//
//			// Poll output frames from the audio decoder.
//			// Do not poll if we already have a pending buffer to feed to the
//			// encoder.
//			while (mNeedCopyAudio && !audioDecoderDone && pendingAudioDecoderOutputBufferIndex == -1 && (encoderOutputAudioFormat == null || muxing)) {
//				int decoderOutputBufferIndex = audioDecoder.dequeueOutputBuffer(audioDecoderOutputBufferInfo, TIMEOUT_USEC);
//				if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
//					if (VERBOSE)
//						Log.d(TAG, "no audio decoder output buffer");
//					break;
//				}
//				if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//					if (VERBOSE)
//						Log.d(TAG, "audio decoder: output buffers changed");
//					audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
//					break;
//				}
//				if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//					decoderOutputAudioFormat = audioDecoder.getOutputFormat();
//					if (VERBOSE) {
//						Log.d(TAG, "audio decoder: output format changed: " + decoderOutputAudioFormat);
//					}
//					break;
//				}
//				if (VERBOSE) {
//					Log.d(TAG, "audio decoder: returned output buffer: " + decoderOutputBufferIndex);
//				}
//				if (VERBOSE) {
//					Log.d(TAG, "audio decoder: returned buffer of size " + audioDecoderOutputBufferInfo.size);
//				}
//				ByteBuffer decoderOutputBuffer = audioDecoderOutputBuffers[decoderOutputBufferIndex];
//				if ((audioDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//					if (VERBOSE)
//						Log.d(TAG, "audio decoder: codec config buffer");
//					audioDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);
//					break;
//				}
//				if (VERBOSE) {
//					Log.d(TAG, "audio decoder: returned buffer for time " + audioDecoderOutputBufferInfo.presentationTimeUs);
//				}
//				if (VERBOSE) {
//					Log.d(TAG, "audio decoder: output buffer is now pending: " + pendingAudioDecoderOutputBufferIndex);
//				}
//				pendingAudioDecoderOutputBufferIndex = decoderOutputBufferIndex;
//				audioDecodedFrameCount++;
//				// We extracted a pending frame, let's try something else next.
//				break;
//			}
//
//			// Feed the pending decoded audio buffer to the audio encoder.
//			while (mNeedCopyAudio && pendingAudioDecoderOutputBufferIndex != -1) {
//				if (VERBOSE) {
//					Log.d(TAG, "audio decoder: attempting to process pending buffer: " + pendingAudioDecoderOutputBufferIndex);
//				}
//				int encoderInputBufferIndex = audioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
//				if (encoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
//					if (VERBOSE)
//						Log.d(TAG, "no audio encoder input buffer");
//					break;
//				}
//				if (VERBOSE) {
//					Log.d(TAG, "audio encoder: returned input buffer: " + encoderInputBufferIndex);
//				}
//				ByteBuffer encoderInputBuffer = audioEncoderInputBuffers[encoderInputBufferIndex];
//				int size = audioDecoderOutputBufferInfo.size;
//				long presentationTime = audioDecoderOutputBufferInfo.presentationTimeUs;
//				if (VERBOSE) {
//					Log.d(TAG, "audio decoder: processing pending buffer: " + pendingAudioDecoderOutputBufferIndex);
//				}
//				if (VERBOSE) {
//					Log.d(TAG, "audio decoder: pending buffer of size " + size);
//					Log.d(TAG, "audio decoder: pending buffer for time " + presentationTime);
//				}
//				if (size >= 0) {
//					ByteBuffer decoderOutputBuffer = audioDecoderOutputBuffers[pendingAudioDecoderOutputBufferIndex].duplicate();
//					decoderOutputBuffer.position(audioDecoderOutputBufferInfo.offset);
//					decoderOutputBuffer.limit(audioDecoderOutputBufferInfo.offset + size);
//					encoderInputBuffer.position(0);
//					encoderInputBuffer.put(decoderOutputBuffer);
//
//					audioEncoder.queueInputBuffer(encoderInputBufferIndex, 0, size, presentationTime, audioDecoderOutputBufferInfo.flags);
//				}
//				audioDecoder.releaseOutputBuffer(pendingAudioDecoderOutputBufferIndex, false);
//				pendingAudioDecoderOutputBufferIndex = -1;
//				if ((audioDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//					if (VERBOSE)
//						Log.d(TAG, "audio decoder: EOS");
//					audioDecoderDone = true;
//				}
//				// We enqueued a pending frame, let's try something else next.
//				break;
//			}
//
//			// Poll frames from the video encoder and send them to the muxer.
//			while (mNeedCopyVideo && !videoEncoderDone && (encoderOutputVideoFormat == null || muxing)) {
//				int encoderOutputBufferIndex = videoEncoder.dequeueOutputBuffer(videoEncoderOutputBufferInfo, TIMEOUT_USEC);
//				if (encoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
//					if (VERBOSE)
//						Log.d(TAG, "no video encoder output buffer");
//					break;
//				}
//				if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//					if (VERBOSE)
//						Log.d(TAG, "video encoder: output buffers changed");
//					videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
//					break;
//				}
//				if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//					if (VERBOSE)
//						Log.d(TAG, "video encoder: output format changed");
//					if (outputVideoTrack >= 0) {
//						fail("video encoder changed its output format again?");
//					}
//					encoderOutputVideoFormat = videoEncoder.getOutputFormat();
//					break;
//				}
//				assertTrue("should have added track before processing output", muxing);
//				if (VERBOSE) {
//					Log.d(TAG, "video encoder: returned output buffer: " + encoderOutputBufferIndex);
//					Log.d(TAG, "video encoder: returned buffer of size " + videoEncoderOutputBufferInfo.size);
//				}
//				ByteBuffer encoderOutputBuffer = videoEncoderOutputBuffers[encoderOutputBufferIndex];
//				if ((videoEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//					if (VERBOSE)
//						Log.d(TAG, "video encoder: codec config buffer");
//					// Simply ignore codec config buffers.
//					videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
//					break;
//				}
//				if (VERBOSE) {
//					Log.d(TAG, "video encoder: returned buffer for time " + videoEncoderOutputBufferInfo.presentationTimeUs);
//				}
//				if (videoEncoderOutputBufferInfo.size != 0) {
//					muxer.writeSampleData(outputVideoTrack, encoderOutputBuffer, videoEncoderOutputBufferInfo);
//				}
//				if ((videoEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//					if (VERBOSE)
//						Log.d(TAG, "video encoder: EOS");
//					videoEncoderDone = true;
//				}
//				videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
//				videoEncodedFrameCount++;
//				// We enqueued an encoded frame, let's try something else next.
//				break;
//			}
//
//			// Poll frames from the audio encoder and send them to the muxer.
//			while (mNeedCopyAudio && !audioEncoderDone && (encoderOutputAudioFormat == null || muxing)) {
//				int encoderOutputBufferIndex = audioEncoder.dequeueOutputBuffer(audioEncoderOutputBufferInfo, TIMEOUT_USEC);
//				if (encoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
//					if (VERBOSE)
//						Log.d(TAG, "no audio encoder output buffer");
//					break;
//				}
//				if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//					if (VERBOSE)
//						Log.d(TAG, "audio encoder: output buffers changed");
//					audioEncoderOutputBuffers = audioEncoder.getOutputBuffers();
//					break;
//				}
//				if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//					if (VERBOSE)
//						Log.d(TAG, "audio encoder: output format changed");
//					if (outputAudioTrack >= 0) {
//						fail("audio encoder changed its output format again?");
//					}
//
//					encoderOutputAudioFormat = audioEncoder.getOutputFormat();
//					break;
//				}
//				assertTrue("should have added track before processing output", muxing);
//				if (VERBOSE) {
//					Log.d(TAG, "audio encoder: returned output buffer: " + encoderOutputBufferIndex);
//					Log.d(TAG, "audio encoder: returned buffer of size " + audioEncoderOutputBufferInfo.size);
//				}
//				ByteBuffer encoderOutputBuffer = audioEncoderOutputBuffers[encoderOutputBufferIndex];
//				if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//					if (VERBOSE)
//						Log.d(TAG, "audio encoder: codec config buffer");
//					// Simply ignore codec config buffers.
//					audioEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
//					break;
//				}
//				if (VERBOSE) {
//					Log.d(TAG, "audio encoder: returned buffer for time " + audioEncoderOutputBufferInfo.presentationTimeUs);
//				}
//				if (audioEncoderOutputBufferInfo.size != 0) {
//					muxer.writeSampleData(outputAudioTrack, encoderOutputBuffer, audioEncoderOutputBufferInfo);
//				}
//				if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//					if (VERBOSE)
//						Log.d(TAG, "audio encoder: EOS");
//					audioEncoderDone = true;
//				}
//				audioEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
//				audioEncodedFrameCount++;
//				// We enqueued an encoded frame, let's try something else next.
//				break;
//			}
//
//			if (!muxing && (!mNeedCopyAudio || encoderOutputAudioFormat != null) && (!mNeedCopyVideo || encoderOutputVideoFormat != null)) {
//				if (mNeedCopyVideo) {
//					Log.d(TAG, "muxer: adding video track.");
//					outputVideoTrack = muxer.addTrack(encoderOutputVideoFormat);
//				}
//				if (mNeedCopyAudio) {
//					Log.d(TAG, "muxer: adding audio track.");
//					outputAudioTrack = muxer.addTrack(encoderOutputAudioFormat);
//				}
//				Log.d(TAG, "muxer: starting");
//				muxer.start();
//				muxing = true;
//			}
//		}
//
//		// Basic sanity checks.
//		if (mNeedCopyVideo) {
//			assertEquals("encoded and decoded video frame counts should match", videoDecodedFrameCount, videoEncodedFrameCount);
//			assertTrue("decoded frame count should be less than extracted frame count", videoDecodedFrameCount <= videoExtractedFrameCount);
//		}
//		if (mNeedCopyAudio) {
//			assertEquals("no frame should be pending", -1, pendingAudioDecoderOutputBufferIndex);
//		}
//
//	}
//}
