package com.yy.lvf.transcoder;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;
import net.ypresto.androidtranscoder.engine.InputSurface;
import net.ypresto.androidtranscoder.engine.OutputSurface;
import net.ypresto.androidtranscoder.format.CompressFileSizeFormatStrategy;
import net.ypresto.androidtranscoder.format.MediaFormatStrategyPresets;

@TargetApi(18)
public class ExtractDecodeEditEncodeMuxTest {

	private static final String TAG = ExtractDecodeEditEncodeMuxTest.class.getSimpleName();
	private static final boolean VERBOSE = true;

	private static final int TIMEOUT_USEC = 10000;

	private static final String OUTPUT_VIDEO_MIME_TYPE = "video/avc"; // 视频输出mime，可指定，含义(H.264 Advanced Video Coding)
	private static final int OUTPUT_VIDEO_BIT_RATE = 1000000;
	private static final int OUTPUT_VIDEO_FRAME_RATE = 15; // 15fps
	private static final int OUTPUT_VIDEO_IFRAME_INTERVAL = 30; // I帧间隔
	private static final int OUTPUT_VIDEO_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
	// private static final String OUTPUT_VIDEO_MIME_TYPE = "video/avc"; // 视频输出mime，可指定，含义(H.264 Advanced Video Coding)
	// private static final int OUTPUT_VIDEO_BIT_RATE = 2000000; // 2Mbps
	// private static final int OUTPUT_VIDEO_FRAME_RATE = 15; // 15fps
	// private static final int OUTPUT_VIDEO_IFRAME_INTERVAL = 10; // I帧间隔
	// private static final int OUTPUT_VIDEO_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

	private static final String OUTPUT_AUDIO_MIME_TYPE = "audio/mp4a-latm";
	private static final int OUTPUT_AUDIO_CHANNEL_COUNT = 2;
	private static final int OUTPUT_AUDIO_BIT_RATE = 128000;
	private static final int OUTPUT_AUDIO_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectHE;
	private static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 48000; // 必须和输入流一致
	// private static final String OUTPUT_AUDIO_MIME_TYPE = "audio/mp4a-latm";
	// private static final int OUTPUT_AUDIO_CHANNEL_COUNT = 2;
	// private static final int OUTPUT_AUDIO_BIT_RATE = 128 * 1024;
	// private static final int OUTPUT_AUDIO_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectHE;
	// private static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 44100; // 必须和输入流一致

	/**
	 * Used for editing the frames.
	 * <p>
	 * Swaps green and blue channels by storing an RBGA color in an RGBA buffer.
	 */
	private static final String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n" +
		"precision mediump float;\n" +
		"varying vec2 vTextureCoord;\n" +
		"uniform samplerExternalOES sTexture;\n" +
		"void main() {\n" +
		"  gl_FragColor = texture2D(sTexture, vTextureCoord).rbga;\n" +
		"}\n";

	private boolean mCopyVideo = false;
	private boolean mCopyAudio = false;
	private int mWidth = -1;// 输出
	private int mHeight = -1;// 输出

	private File mInputF;// 输入文件
	private File mOutputF;// 输出文件

	public void testExtractDecodeEditEncodeMuxQCIF(File input, File output) throws Throwable {
		setSize(176, 144);
		setSource(input, output);
		setCopyVideo();
		TestWrapper.runTest(this);
	}

	public void testExtractDecodeEditEncodeMuxQVGA(File input, File output) throws Throwable {
		setSize(320, 240);
		setSource(input, output);
		setCopyVideo();
		TestWrapper.runTest(this);
	}

	public void testExtractDecodeEditEncodeMux720p(File input, File output) throws Throwable {
		setSize(1280, 720);
		setSource(input, output);
		setCopyVideo();
		TestWrapper.runTest(this);
	}

	public void testExtractDecodeEditEncodeMuxAudio(File input, File output) throws Throwable {
		setSize(1280, 720);
		setSource(input, output);
		setCopyAudio();
		TestWrapper.runTest(this);
	}

	public void testExtractDecodeEditEncodeMuxAudioVideo(File input, File output) throws Throwable {
		setSize(480, 480);
		setSource(input, output);
		// setCopyAudio();
		setCopyVideo();
		TestWrapper.runTest(this);
	}

	private static class TestWrapper implements Runnable {
		private Throwable mThrowable;
		private ExtractDecodeEditEncodeMuxTest mTest;

		private TestWrapper(ExtractDecodeEditEncodeMuxTest test) {
			mTest = test;
		}

		@Override
		public void run() {
			try {
				mTest.extractDecodeEditEncodeMux();
			} catch (Throwable th) {
				mThrowable = th;
			}
		}

		public static void runTest(ExtractDecodeEditEncodeMuxTest test) throws Throwable {
			TestWrapper wrapper = new TestWrapper(test);
			Thread th = new Thread(wrapper, "codec test");
			th.start();
			if (wrapper.mThrowable != null) {
				throw wrapper.mThrowable;
			}
		}
	}

	private void setCopyVideo() {
		mCopyVideo = true;
	}

	private void setCopyAudio() {
		mCopyAudio = true;
	}

	private void setSize(int width, int height) {
		if ((width % 16) != 0 || (height % 16) != 0) {
			Log.w(TAG, "WARNING: width or height not multiple of 16");
		}
		mWidth = width;
		mHeight = height;
	}

	private void setSource(File input, File output) {
		mInputF = input;
		mOutputF = output;
	}

	private void extractDecodeEditEncodeMux() throws Exception {
		Exception exception = null;

		MediaFormat outputVideoFormat = null;
		MediaFormat outputAudioFormat = null;
		// MediaCodecInfo videoCodecInfo = selectCodec(OUTPUT_VIDEO_MIME_TYPE);
		// if (videoCodecInfo == null) {
		// Log.e(TAG, "当前设备不支持" + OUTPUT_VIDEO_MIME_TYPE);
		// return;
		// }
		// if (VERBOSE)
		// Log.d(TAG, "videoCodecInfo: " + videoCodecInfo);
		//
		// MediaCodecInfo audioCodecInfo = selectCodec(OUTPUT_AUDIO_MIME_TYPE);
		// if (audioCodecInfo == null) {
		// Log.e(TAG, "当前设备不支持" + OUTPUT_AUDIO_MIME_TYPE);
		// return;
		// }
		// if (VERBOSE)
		// Log.d(TAG, "audioCodecInfo: " + audioCodecInfo);

		MediaExtractor videoExtractor = null;
		MediaExtractor audioExtractor = null;
		OutputSurface outputSurface = null;
		MediaCodec videoDecoder = null;
		MediaCodec audioDecoder = null;
		MediaCodec videoEncoder = null;
		MediaCodec audioEncoder = null;
		MediaMuxer muxer = null;

		InputSurface inputSurface = null;
		CompressFileSizeFormatStrategy strategy = (CompressFileSizeFormatStrategy) MediaFormatStrategyPresets.createDecreaseBitrateFormatStrategy(600000, 128000, 2);
		try {
			if (mCopyVideo) {
				videoExtractor = createExtractor(mInputF);
				int videoInputTrack = getAndSelectVideoTrackIndex(videoExtractor);
				MediaFormat inputFormat = videoExtractor.getTrackFormat(videoInputTrack);
				strategy.setFrameRate(15);
				strategy.setIFrameInterval(5);
				outputVideoFormat = strategy.createVideoOutputFormat(inputFormat);
				// 编码器
				AtomicReference<Surface> inputSurfaceReference = new AtomicReference<Surface>();
				videoEncoder = createVideoEncoder(outputVideoFormat, inputSurfaceReference);
				inputSurface = new InputSurface(inputSurfaceReference.get());
				inputSurface.makeCurrent();
				// 解码器
				outputSurface = new OutputSurface();
				videoDecoder = createVideoDecoder(inputFormat, outputSurface.getSurface());
			}

			if (mCopyAudio) {
				audioExtractor = createExtractor(mInputF);
				int audioInputTrack = getAndSelectAudioTrackIndex(audioExtractor);
				// assertTrue("missing audio track in test video", audioInputTrack != -1);
				MediaFormat inputFormat = audioExtractor.getTrackFormat(audioInputTrack);
				Log.d(TAG, "inputAudioFormat： " + inputFormat);
				outputAudioFormat = strategy.createAudioOutputFormat(inputFormat);
				Log.d(TAG, "outputAudioFormat： " + outputAudioFormat);
				// 编码器
				audioEncoder = createAudioEncoder(outputAudioFormat);
				// 解码器
				audioDecoder = createAudioDecoder(inputFormat);
			}

			// 创建Muxer但是不要启动和选择轨道
			// Muxer用来写编码之后的帧，只有当所有的流被加进来之后，Muxer才应该启动。
			muxer = createMuxer();

			doExtractDecodeEditEncodeMux(videoExtractor, audioExtractor, videoDecoder, videoEncoder, audioDecoder, audioEncoder, muxer, inputSurface, outputSurface);
		} finally {
			if (VERBOSE)
				Log.d(TAG, "releasing extractor, decoder, encoder, and muxer");
			try {
				if (videoExtractor != null) {
					videoExtractor.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing videoExtractor", e);
				if (exception == null) {
					exception = e;
				}
			}
			try {
				if (audioExtractor != null) {
					audioExtractor.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing audioExtractor", e);
				if (exception == null) {
					exception = e;
				}
			}
			try {
				if (videoDecoder != null) {
					videoDecoder.stop();
					videoDecoder.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing videoDecoder", e);
				if (exception == null) {
					exception = e;
				}
			}
			try {
				if (outputSurface != null) {
					outputSurface.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing outputSurface", e);
				if (exception == null) {
					exception = e;
				}
			}
			try {
				if (videoEncoder != null) {
					videoEncoder.stop();
					videoEncoder.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing videoEncoder", e);
				if (exception == null) {
					exception = e;
				}
			}
			try {
				if (audioDecoder != null) {
					audioDecoder.stop();
					audioDecoder.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing audioDecoder", e);
				if (exception == null) {
					exception = e;
				}
			}
			try {
				if (audioEncoder != null) {
					audioEncoder.stop();
					audioEncoder.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing audioEncoder", e);
				if (exception == null) {
					exception = e;
				}
			}
			try {
				if (muxer != null) {
					muxer.stop();
					muxer.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing muxer", e);
				if (exception == null) {
					exception = e;
				}
			}
			try {
				if (inputSurface != null) {
					inputSurface.release();
				}
			} catch (Exception e) {
				Log.e(TAG, "error while releasing inputSurface", e);
				if (exception == null) {
					exception = e;
				}
			}
		}
		if (exception != null) {
			throw exception;
		}
	}

	private MediaExtractor createExtractor(File input) throws IOException {
		MediaExtractor extractor;
		FileInputStream is = null;
		try {
			is = new FileInputStream(input);
			FileDescriptor fd = is.getFD();
			extractor = new MediaExtractor();
			extractor.setDataSource(fd);
		} finally {
			if (is != null) {
				is.close();
				is = null;
			}
		}
		return extractor;
	}

	private MediaCodec createVideoDecoder(MediaFormat inputFormat, Surface surface) throws IOException {
		MediaCodec decoder = MediaCodec.createDecoderByType(inputFormat.getString(MediaFormat.KEY_MIME));
		decoder.configure(inputFormat, surface, null, 0);
		decoder.start();
		return decoder;
	}

	private MediaCodec createVideoEncoder(MediaFormat format, AtomicReference<Surface> surfaceReference) throws IOException {
		MediaCodec encoder = MediaCodec.createEncoderByType(format.getString(MediaFormat.KEY_MIME));
		encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		surfaceReference.set(encoder.createInputSurface());// start之前调用
		encoder.start();
		return encoder;
	}

	private MediaCodec createAudioDecoder(MediaFormat inputFormat) throws IOException {
		MediaCodec decoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
		decoder.configure(inputFormat, null, null, 0);
		decoder.start();
		return decoder;
	}

	private MediaCodec createAudioEncoder(MediaFormat format) throws IOException {
		MediaCodec encoder = MediaCodec.createEncoderByType(getMimeTypeFor(format));
		encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		encoder.start();
		return encoder;
	}

	private MediaMuxer createMuxer() throws IOException {
		return new MediaMuxer(mOutputF.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
	}

	private int getAndSelectVideoTrackIndex(MediaExtractor extractor) {
		for (int index = 0; index < extractor.getTrackCount(); ++index) {
			if (VERBOSE) {
				Log.d(TAG, "format for track " + index + " is " + getMimeTypeFor(extractor.getTrackFormat(index)));
			}
			if (isVideoFormat(extractor.getTrackFormat(index))) {
				extractor.selectTrack(index);
				return index;
			}
		}
		return -1;
	}

	private int getAndSelectAudioTrackIndex(MediaExtractor extractor) {
		for (int index = 0; index < extractor.getTrackCount(); ++index) {
			if (VERBOSE) {
				Log.d(TAG, "format for track " + index + " is " + getMimeTypeFor(extractor.getTrackFormat(index)));
			}
			if (isAudioFormat(extractor.getTrackFormat(index))) {
				extractor.selectTrack(index);
				return index;
			}
		}
		return -1;
	}

	// extracting, decoding, encoding and muxing
	private void doExtractDecodeEditEncodeMux(MediaExtractor videoExtractor, MediaExtractor audioExtractor, MediaCodec videoDecoder, MediaCodec videoEncoder, MediaCodec audioDecoder, MediaCodec audioEncoder, MediaMuxer muxer, InputSurface inputSurface, OutputSurface outputSurface) {
		// 根据MediaCodec工作的数据流图，每个MediaCodec基本对应两组
		ByteBuffer[] videoDecoderInputBuffers = null;
		ByteBuffer[] videoEncoderOutputBuffers = null;
		MediaCodec.BufferInfo videoDecoderOutputBufferInfo = null;
		MediaCodec.BufferInfo videoEncoderOutputBufferInfo = null;
		if (mCopyVideo) {
			videoDecoderInputBuffers = videoDecoder.getInputBuffers();
			videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
			videoDecoderOutputBufferInfo = new MediaCodec.BufferInfo();
			videoEncoderOutputBufferInfo = new MediaCodec.BufferInfo();
		}
		ByteBuffer[] audioDecoderInputBuffers = null;
		ByteBuffer[] audioDecoderOutputBuffers = null;
		ByteBuffer[] audioEncoderInputBuffers = null;
		ByteBuffer[] audioEncoderOutputBuffers = null;
		MediaCodec.BufferInfo audioDecoderOutputBufferInfo = null;
		MediaCodec.BufferInfo audioEncoderOutputBufferInfo = null;
		if (mCopyAudio) {
			audioDecoderInputBuffers = audioDecoder.getInputBuffers();
			audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
			audioEncoderInputBuffers = audioEncoder.getInputBuffers();
			audioEncoderOutputBuffers = audioEncoder.getOutputBuffers();
			audioDecoderOutputBufferInfo = new MediaCodec.BufferInfo();
			audioEncoderOutputBufferInfo = new MediaCodec.BufferInfo();
		}

		MediaFormat decoderOutputVideoFormat = null;
		MediaFormat decoderOutputAudioFormat = null;

		MediaFormat encoderOutputVideoFormat = null;
		MediaFormat encoderOutputAudioFormat = null;

		// We will determine these once we have the output format.
		int outputVideoTrack = -1;
		int outputAudioTrack = -1;

		boolean videoExtractorDone = false;
		boolean videoDecoderDone = false;
		boolean videoEncoderDone = false;

		boolean audioExtractorDone = false;
		boolean audioDecoderDone = false;
		boolean audioEncoderDone = false;
		// The audio decoder output buffer to process, -1 if none.
		int pendingAudioDecoderOutputBufferIndex = -1;

		boolean muxing = false;

		int videoExtractedFrameCount = 0;
		int videoDecodedFrameCount = 0;
		int videoEncodedFrameCount = 0;

		int audioExtractedFrameCount = 0;
		int audioDecodedFrameCount = 0;
		int audioEncodedFrameCount = 0;
		
		long videoDecoderPresentaionUsSum = 0;

		while ((mCopyVideo && !videoEncoderDone) || (mCopyAudio && !audioEncoderDone)) {
			if (VERBOSE) {
				Log.d(TAG, String.format(
					"loop: "

						+ "V(%b){"
						+ "extracted:%d(done:%b) "
						+ "decoded:%d(done:%b) "
						+ "encoded:%d(done:%b)} "

						+ "A(%b){"
						+ "extracted:%d(done:%b) "
						+ "decoded:%d(done:%b) "
						+ "encoded:%d(done:%b) "
						+ "pending:%d} "

						+ "muxing:%b(V:%d,A:%d)",

					mCopyVideo,
					videoExtractedFrameCount, videoExtractorDone,
					videoDecodedFrameCount, videoDecoderDone,
					videoEncodedFrameCount, videoEncoderDone,

					mCopyAudio,
					audioExtractedFrameCount, audioExtractorDone,
					audioDecodedFrameCount, audioDecoderDone,
					audioEncodedFrameCount, audioEncoderDone,
					pendingAudioDecoderOutputBufferIndex,

					muxing, outputVideoTrack, outputAudioTrack));
			}

			// 视频提取 ---> 解码（输入）
			while (mCopyVideo && !videoExtractorDone && (encoderOutputVideoFormat == null || muxing)) {
				int decoderInputBufferIndex = videoDecoder.dequeueInputBuffer(TIMEOUT_USEC);// 请求输入buffer
				if (decoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)
						Log.d(TAG, "no video decoder input buffer");
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "video decoder: returned input buffer: " + decoderInputBufferIndex);
				}
				ByteBuffer decoderInputBuffer = videoDecoderInputBuffers[decoderInputBufferIndex];
				int size = videoExtractor.readSampleData(decoderInputBuffer, 0);
				long presentationTime = videoExtractor.getSampleTime();
				if (VERBOSE) {
					Log.d(TAG, "video extractor: returned buffer of size " + size);
					Log.d(TAG, "video extractor: returned buffer for time " + presentationTime);
				}
				if (size >= 0) {
					videoDecoder.queueInputBuffer(decoderInputBufferIndex, 0, size, presentationTime, videoExtractor.getSampleFlags());
					videoDecoderPresentaionUsSum += presentationTime;
				}
				videoExtractorDone = !videoExtractor.advance();// 当前采样是否提取完
				if (videoExtractorDone) {
					if (VERBOSE) {
						Log.d(TAG, "video extractor: EOS");
					}
					videoDecoder.queueInputBuffer(decoderInputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				}
				videoExtractedFrameCount++;
				break;
			}

			// 音频提取 ---> 解码（输入）
			while (mCopyAudio && !audioExtractorDone && (encoderOutputAudioFormat == null || muxing)) {
				int decoderInputBufferIndex = audioDecoder.dequeueInputBuffer(TIMEOUT_USEC);
				if (decoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)
						Log.d(TAG, "no audio decoder input buffer");
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: returned input buffer: " + decoderInputBufferIndex);
				}
				ByteBuffer decoderInputBuffer = audioDecoderInputBuffers[decoderInputBufferIndex];
				int size = audioExtractor.readSampleData(decoderInputBuffer, 0);
				long presentationTime = audioExtractor.getSampleTime();
				if (VERBOSE) {
					Log.d(TAG, "audio extractor: returned buffer of size " + size);
					Log.d(TAG, "audio extractor: returned buffer for time " + presentationTime);
				}
				if (size >= 0) {
					audioDecoder.queueInputBuffer(decoderInputBufferIndex, 0, size, presentationTime, audioExtractor.getSampleFlags());
				}
				audioExtractorDone = !audioExtractor.advance();
				if (audioExtractorDone) {
					if (VERBOSE)
						Log.d(TAG, "audio extractor: EOS");
					audioDecoder.queueInputBuffer(decoderInputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				}
				audioExtractedFrameCount++;
				break;
			}

			// 视频解码（输出）---> 编码（输入）
			while (mCopyVideo && !videoDecoderDone && (encoderOutputVideoFormat == null || muxing)) {
				int decoderOutputBufferIndex = videoDecoder.dequeueOutputBuffer(videoDecoderOutputBufferInfo, TIMEOUT_USEC);
				if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)
						Log.d(TAG, "no video decoder output buffer");
					break;
				}
				if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					if (VERBOSE)
						Log.d(TAG, "video decoder: output buffers changed");
					break;
				}
				if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					decoderOutputVideoFormat = videoDecoder.getOutputFormat();
					if (VERBOSE) {
						Log.d(TAG, "video decoder: output format changed: " + decoderOutputVideoFormat);
					}
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "video decoder: returned output buffer: " + decoderOutputBufferIndex);
					Log.d(TAG, "video decoder: returned buffer of size " + videoDecoderOutputBufferInfo.size);
					Log.d(TAG, "video decoder: returned buffer for time " + videoDecoderOutputBufferInfo.presentationTimeUs);
				}
				boolean render = (videoDecoderOutputBufferInfo.size > 0);
				// 过滤配置buffer
				if ((videoDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					if (VERBOSE) {
						Log.d(TAG, "video decoder: codec config buffer");
					}
					videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex, render);
					break;
				}
				videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex, render);
				if (render) {
					if (VERBOSE) {
						Log.d(TAG, "output surface: await new image");
					}
					outputSurface.awaitNewImage();
					if (VERBOSE) {
						Log.d(TAG, "output surface: draw image");
					}
					outputSurface.drawImage();// 把数据从SurfaceTexture画到当前EGLSurface
					inputSurface.setPresentationTime(videoDecoderOutputBufferInfo.presentationTimeUs * 1000);
					if (VERBOSE) {
						Log.d(TAG, "input surface: swap buffers");
					}
					inputSurface.swapBuffers();// 调用EGLSurface数据
					if (VERBOSE) {
						Log.d(TAG, "video encoder: notified of new frame");
					}
				}
				if ((videoDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (VERBOSE)
						Log.d(TAG, "video decoder: EOS");
					videoDecoderDone = true;
					videoEncoder.signalEndOfInputStream();
				}
				videoDecodedFrameCount++;
				break;
			}

			// 音频解码（输出）---> 编码（输入）
			while (mCopyAudio && !audioDecoderDone && pendingAudioDecoderOutputBufferIndex == -1 && (encoderOutputAudioFormat == null || muxing)) {
				int decoderOutputBufferIndex = audioDecoder.dequeueOutputBuffer(audioDecoderOutputBufferInfo, TIMEOUT_USEC);
				if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE) {
						Log.d(TAG, "no audio decoder output buffer");
					}
					break;
				}
				if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					if (VERBOSE) {
						Log.d(TAG, "audio decoder: output buffers changed");
					}
					audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
					break;
				}
				if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					decoderOutputAudioFormat = audioDecoder.getOutputFormat();
					if (VERBOSE) {
						Log.d(TAG, "audio decoder: output format changed: " + decoderOutputAudioFormat);
					}
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: returned output buffer: " + decoderOutputBufferIndex);
					Log.d(TAG, "audio decoder: returned buffer of size " + audioDecoderOutputBufferInfo.size);
				}
				if ((audioDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					if (VERBOSE)
						Log.d(TAG, "audio decoder: codec config buffer");
					audioDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: returned buffer for time " + audioDecoderOutputBufferInfo.presentationTimeUs);
					Log.d(TAG, "audio decoder: output buffer is now pending: " + pendingAudioDecoderOutputBufferIndex);
				}
				pendingAudioDecoderOutputBufferIndex = decoderOutputBufferIndex;
				audioDecodedFrameCount++;
				break;
			}

			// 将待处理的已解码音频数据喂给音频编码器
			while (mCopyAudio && pendingAudioDecoderOutputBufferIndex != -1) {
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: attempting to process pending buffer: " + pendingAudioDecoderOutputBufferIndex);
				}
				int encoderInputBufferIndex = audioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
				if (encoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE) {
						Log.d(TAG, "no audio encoder input buffer");
					}
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "audio encoder: returned input buffer: " + encoderInputBufferIndex);
				}
				ByteBuffer encoderInputBuffer = audioEncoderInputBuffers[encoderInputBufferIndex];
				int size = audioDecoderOutputBufferInfo.size;
				long presentationTime = audioDecoderOutputBufferInfo.presentationTimeUs;
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: processing pending buffer: " + pendingAudioDecoderOutputBufferIndex);
				}
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: pending buffer of size " + size);
					Log.d(TAG, "audio decoder: pending buffer for time " + presentationTime);
				}
				if (size >= 0) {
					ByteBuffer decoderOutputBuffer = audioDecoderOutputBuffers[pendingAudioDecoderOutputBufferIndex].duplicate();
					decoderOutputBuffer.position(audioDecoderOutputBufferInfo.offset);
					decoderOutputBuffer.limit(audioDecoderOutputBufferInfo.offset + size);
					encoderInputBuffer.position(0);
					encoderInputBuffer.put(decoderOutputBuffer);

					audioEncoder.queueInputBuffer(
						encoderInputBufferIndex,
						0,
						size,
						presentationTime,
						audioDecoderOutputBufferInfo.flags);
				}
				audioDecoder.releaseOutputBuffer(pendingAudioDecoderOutputBufferIndex, false);
				pendingAudioDecoderOutputBufferIndex = -1;
				if ((audioDecoderOutputBufferInfo.flags
					& MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (VERBOSE) {
						Log.d(TAG, "audio decoder: EOS");
					}
					audioDecoderDone = true;
				}
				break;
			}

			// 视频编码（输出）---> 封装
			while (mCopyVideo && !videoEncoderDone && (encoderOutputVideoFormat == null || muxing)) {
				int encoderOutputBufferIndex = videoEncoder.dequeueOutputBuffer(videoEncoderOutputBufferInfo, TIMEOUT_USEC);
				if (encoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)
						Log.d(TAG, "no video encoder output buffer");
					break;
				}
				if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					if (VERBOSE)
						Log.d(TAG, "video encoder: output buffers changed");
					videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
					break;
				}
				if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					if (VERBOSE)
						Log.d(TAG, "video encoder: output format changed");
					if (outputVideoTrack >= 0 || muxing) {
						throw new RuntimeException("format changed twice");
					}
					encoderOutputVideoFormat = videoEncoder.getOutputFormat();
					outputVideoTrack = muxer.addTrack(encoderOutputVideoFormat);
					muxer.start();
					muxing = true;
					break;
				}
				if (encoderOutputBufferIndex < 0) {
					Log.d(TAG, "video encoder: unexpected result from encoder.dequeueOutputBuffer: " + encoderOutputBufferIndex);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "video encoder: returned output buffer: " + encoderOutputBufferIndex);
					Log.d(TAG, "video encoder: returned buffer of size " + videoEncoderOutputBufferInfo.size);
					Log.d(TAG, "video encoder: returned buffer for time " + videoEncoderOutputBufferInfo.presentationTimeUs);
				}
				ByteBuffer encoderOutputBuffer = videoEncoderOutputBuffers[encoderOutputBufferIndex];
				if ((videoEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					if (VERBOSE)
						Log.d(TAG, "video encoder: codec config buffer");
					videoEncoderOutputBufferInfo.size = 0;
				}
				if (videoEncoderOutputBufferInfo.size != 0) {
					if (!muxing) {
						throw new RuntimeException("muxer hasn't started");
					}
					encoderOutputBuffer.position(videoEncoderOutputBufferInfo.offset);
					encoderOutputBuffer.limit(videoEncoderOutputBufferInfo.offset + videoEncoderOutputBufferInfo.size);
					muxer.writeSampleData(outputVideoTrack, encoderOutputBuffer, videoEncoderOutputBufferInfo);
				}
				if ((videoEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (VERBOSE)
						Log.d(TAG, "video encoder: EOS");
					videoEncoderDone = true;
				}
				videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
				videoEncodedFrameCount++;
				break;
			}

			// 从音频编码器中获取帧并发送给Muxer
			while (mCopyAudio && !audioEncoderDone && (encoderOutputAudioFormat == null || muxing)) {
				int encoderOutputBufferIndex = audioEncoder.dequeueOutputBuffer(audioEncoderOutputBufferInfo, TIMEOUT_USEC);
				if (encoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)
						Log.d(TAG, "no audio encoder output buffer");
					break;
				}
				if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					if (VERBOSE)
						Log.d(TAG, "audio encoder: output buffers changed");
					audioEncoderOutputBuffers = audioEncoder.getOutputBuffers();
					break;
				}
				if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					if (VERBOSE)
						Log.d(TAG, "audio encoder: output format changed");
					if (outputAudioTrack >= 0) {
						// fail("audio encoder changed its output format again?");
					}

					encoderOutputAudioFormat = audioEncoder.getOutputFormat();
					break;
				}
				// assertTrue("should have added track before processing output", muxing);
				if (VERBOSE) {
					Log.d(TAG, "audio encoder: returned output buffer: " + encoderOutputBufferIndex);
					Log.d(TAG, "audio encoder: returned buffer of size " + audioEncoderOutputBufferInfo.size);
				}
				ByteBuffer encoderOutputBuffer = audioEncoderOutputBuffers[encoderOutputBufferIndex];
				if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					if (VERBOSE)
						Log.d(TAG, "audio encoder: codec config buffer");
					// 对于codec的配置buffer，简单的忽略。
					audioEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "audio encoder: returned buffer for time "
						+ audioEncoderOutputBufferInfo.presentationTimeUs);
				}
				if (audioEncoderOutputBufferInfo.size != 0) {
					muxer.writeSampleData(outputAudioTrack, encoderOutputBuffer, audioEncoderOutputBufferInfo);
				}
				if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (VERBOSE)
						Log.d(TAG, "audio encoder: EOS");
					audioEncoderDone = true;
				}
				audioEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
				audioEncodedFrameCount++;
				break;
			}

			if (!muxing && (!mCopyAudio || encoderOutputAudioFormat != null) && (!mCopyVideo || encoderOutputVideoFormat != null)) {
				if (mCopyVideo) {
					Log.d(TAG, "muxer: adding video track.");
					outputVideoTrack = muxer.addTrack(encoderOutputVideoFormat);
				}
				if (mCopyAudio) {
					Log.d(TAG, "muxer: adding audio track.");
					outputAudioTrack = muxer.addTrack(encoderOutputAudioFormat);
				}
				Log.d(TAG, "muxer: starting");
				muxer.start();
				muxing = true;
			}
		}

		// Basic sanity checks.
		if (mCopyVideo) {
			// assertEquals("encoded and decoded video frame counts should match", videoDecodedFrameCount,
			// videoEncodedFrameCount);
			// assertTrue("decoded frame count should be less than extracted frame count", videoDecodedFrameCount <=
			// videoExtractedFrameCount);
		}
		if (mCopyAudio) {
			// assertEquals("no frame should be pending", -1, pendingAudioDecoderOutputBufferIndex);
		}
		Log.d(TAG, "videoDecoderPresentaionUsSum: " + videoDecoderPresentaionUsSum);
		// TODO: Check the generated output file.
	}

	private static boolean isVideoFormat(MediaFormat format) {
		return getMimeTypeFor(format).startsWith("video/");
	}

	private static boolean isAudioFormat(MediaFormat format) {
		return getMimeTypeFor(format).startsWith("audio/");
	}

	private static String getMimeTypeFor(MediaFormat format) {
		return format.getString(MediaFormat.KEY_MIME);
	}

	// 获取当前设备支持的所有MediaCode信息，并找到支持目标mime的编码器
	public static MediaCodecInfo selectCodec(String mimeType) {
		int numCodecs = MediaCodecList.getCodecCount();
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
			if (!codecInfo.isEncoder()) {
				continue;
			}
			String[] types = codecInfo.getSupportedTypes();
			for (int j = 0; j < types.length; j++) {
				if (types[j].equalsIgnoreCase(mimeType)) {
					return codecInfo;
				}
			}
		}
		return null;
	}

}
