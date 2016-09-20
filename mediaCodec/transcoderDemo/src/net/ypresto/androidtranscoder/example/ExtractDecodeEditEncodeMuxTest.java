package net.ypresto.androidtranscoder.example;

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
import android.os.Environment;
import android.test.AndroidTestCase;
import android.util.Log;
import android.view.Surface;

@TargetApi(18)
public class ExtractDecodeEditEncodeMuxTest extends AndroidTestCase {

	private static final String TAG = ExtractDecodeEditEncodeMuxTest.class.getSimpleName();
	private static final boolean VERBOSE = false; // lots of logging

	/** How long to wait for the next buffer to become available. */
	private static final int TIMEOUT_USEC = 10000;

	/** Where to output the test files. */
	private static final File OUTPUT_FILENAME_DIR = Environment.getExternalStorageDirectory();

	// parameters for the video encoder
	private static final String OUTPUT_VIDEO_MIME_TYPE = "video/avc"; // 视频输出mime，可指定，含义(H.264 Advanced Video Coding)
	private static final int OUTPUT_VIDEO_BIT_RATE = 2000000; // 2Mbps
	private static final int OUTPUT_VIDEO_FRAME_RATE = 15; // 15fps
	private static final int OUTPUT_VIDEO_IFRAME_INTERVAL = 10; // I帧间隔
	private static final int OUTPUT_VIDEO_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

	// parameters for the audio encoder
	private static final String OUTPUT_AUDIO_MIME_TYPE = "audio/mp4a-latm"; // Advanced Audio Coding
	private static final int OUTPUT_AUDIO_CHANNEL_COUNT = 2; // Must match the input stream.
	private static final int OUTPUT_AUDIO_BIT_RATE = 128 * 1024;
	private static final int OUTPUT_AUDIO_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectHE;
	private static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 44100; // Must match the input stream.

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

	private boolean mCopyVideo;
	private boolean mCopyAudio;
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
		setSize(1280, 720);
		setSource(input, output);
		setCopyAudio();
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
			th.join();
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
		MediaCodecInfo videoCodecInfo = selectCodec(OUTPUT_VIDEO_MIME_TYPE);
		if (videoCodecInfo == null) {
			Log.e(TAG, "当前设备不支持" + OUTPUT_VIDEO_MIME_TYPE);
			return;
		}
		if (VERBOSE)
			Log.d(TAG, "videoCodecInfo = " + videoCodecInfo);

		MediaCodecInfo audioCodecInfo = selectCodec(OUTPUT_AUDIO_MIME_TYPE);
		if (audioCodecInfo == null) {
			Log.e(TAG, "当前设备不支持" + OUTPUT_AUDIO_MIME_TYPE);
			return;
		}
		if (VERBOSE)
			Log.d(TAG, "audioCodecInfo" + audioCodecInfo);
		// 视频编码强制属性（MediaFormat有说明）
		outputVideoFormat = MediaFormat.createVideoFormat(OUTPUT_VIDEO_MIME_TYPE, mWidth, mHeight);
		outputVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, OUTPUT_VIDEO_COLOR_FORMAT);
		outputVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, OUTPUT_VIDEO_BIT_RATE);
		outputVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, OUTPUT_VIDEO_FRAME_RATE);
		outputVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, OUTPUT_VIDEO_IFRAME_INTERVAL);
		if (VERBOSE)
			Log.d(TAG, "outputVideoFormat = " + outputVideoFormat);
		// 音频编码强制属性（MediaFormat有说明）
		outputAudioFormat = MediaFormat.createAudioFormat(OUTPUT_AUDIO_MIME_TYPE, OUTPUT_AUDIO_SAMPLE_RATE_HZ, OUTPUT_AUDIO_CHANNEL_COUNT);
		outputAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, OUTPUT_AUDIO_BIT_RATE);
		outputAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, OUTPUT_AUDIO_AAC_PROFILE);
		if (VERBOSE)
			Log.d(TAG, "outputAudioFormat = " + outputAudioFormat);
		// 以上，设置输出属性，需要做足够的兼容性设置，包括mime，分辨率等等。强制属性设置失败会导致MediaCodec调用configure()方法抛unhelpful异常。
		// 当兼容性出现问题时，就应当切换到软编解码方案。

		MediaExtractor videoExtractor = null;
		MediaExtractor audioExtractor = null;
		OutputSurface outputSurface = null;
		MediaCodec videoDecoder = null;
		MediaCodec audioDecoder = null;
		MediaCodec videoEncoder = null;
		MediaCodec audioEncoder = null;
		MediaMuxer muxer = null;

		InputSurface inputSurface = null;

		try {
			if (mCopyVideo) {
				videoExtractor = createExtractor(mInputF);
				int videoInputTrack = getAndSelectVideoTrackIndex(videoExtractor);
				assertTrue("missing video track in test video", videoInputTrack != -1);
				MediaFormat inputFormat = videoExtractor.getTrackFormat(videoInputTrack);
				// 编码器
				AtomicReference<Surface> inputSurfaceReference = new AtomicReference<Surface>();
				videoEncoder = createVideoEncoder(videoCodecInfo, outputVideoFormat, inputSurfaceReference);
				inputSurface = new InputSurface(inputSurfaceReference.get());
				inputSurface.makeCurrent();
				// 解码器
				outputSurface = new OutputSurface();
				outputSurface.changeFragmentShader(FRAGMENT_SHADER);
				videoDecoder = createVideoDecoder(inputFormat, outputSurface.getSurface());
			}

			if (mCopyAudio) {
				audioExtractor = createExtractor(mInputF);
				int audioInputTrack = getAndSelectAudioTrackIndex(audioExtractor);
				assertTrue("missing audio track in test video", audioInputTrack != -1);
				MediaFormat inputFormat = audioExtractor.getTrackFormat(audioInputTrack);

				// 编码器
				audioEncoder = createAudioEncoder(audioCodecInfo, outputAudioFormat);
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
			// Try to release everything we acquired, even if one of the releases fails, in which
			// case we save the first exception we got and re-throw at the end (unless something
			// other exception has already been thrown). This guarantees the first exception thrown
			// is reported as the cause of the error, everything is (attempted) to be released, and
			// all other exceptions appear in the logs.
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
		MediaCodec decoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
		decoder.configure(inputFormat, surface, null, 0);
		decoder.start();
		return decoder;
	}

	private MediaCodec createVideoEncoder(MediaCodecInfo codecInfo, MediaFormat format, AtomicReference<Surface> surfaceReference) throws IOException {
		MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
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

	private MediaCodec createAudioEncoder(MediaCodecInfo codecInfo, MediaFormat format) throws IOException {
		MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
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
				Log.d(TAG, "format for track " + index + " is "
					+ getMimeTypeFor(extractor.getTrackFormat(index)));
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
				Log.d(TAG, "format for track " + index + " is "
					+ getMimeTypeFor(extractor.getTrackFormat(index)));
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
		ByteBuffer[] videoDecoderInputBuffers = null;
		ByteBuffer[] videoDecoderOutputBuffers = null;
		ByteBuffer[] videoEncoderOutputBuffers = null;
		MediaCodec.BufferInfo videoDecoderOutputBufferInfo = null;
		MediaCodec.BufferInfo videoEncoderOutputBufferInfo = null;
		if (mCopyVideo) {
			videoDecoderInputBuffers = videoDecoder.getInputBuffers();
			videoDecoderOutputBuffers = videoDecoder.getOutputBuffers();
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

			while (mCopyVideo && !videoExtractorDone && (encoderOutputVideoFormat == null || muxing)) {
				int decoderInputBufferIndex = videoDecoder.dequeueInputBuffer(TIMEOUT_USEC);
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
					videoDecoder.queueInputBuffer(
						decoderInputBufferIndex,
						0,
						size,
						presentationTime,
						videoExtractor.getSampleFlags());
				}
				videoExtractorDone = !videoExtractor.advance();
				if (videoExtractorDone) {
					if (VERBOSE)
						Log.d(TAG, "video extractor: EOS");
					videoDecoder.queueInputBuffer(
						decoderInputBufferIndex,
						0,
						0,
						0,
						MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				}
				videoExtractedFrameCount++;
				// We extracted a frame, let's try something else next.
				break;
			}

			// Extract audio from file and feed to decoder.
			// Do not extract audio if we have determined the output format but we are not yet
			// ready to mux the frames.
			while (mCopyAudio && !audioExtractorDone
				&& (encoderOutputAudioFormat == null || muxing)) {
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
					audioDecoder.queueInputBuffer(
						decoderInputBufferIndex,
						0,
						size,
						presentationTime,
						audioExtractor.getSampleFlags());
				}
				audioExtractorDone = !audioExtractor.advance();
				if (audioExtractorDone) {
					if (VERBOSE)
						Log.d(TAG, "audio extractor: EOS");
					audioDecoder.queueInputBuffer(
						decoderInputBufferIndex,
						0,
						0,
						0,
						MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				}
				audioExtractedFrameCount++;
				// We extracted a frame, let's try something else next.
				break;
			}

			// Poll output frames from the video decoder and feed the encoder.
			while (mCopyVideo && !videoDecoderDone
				&& (encoderOutputVideoFormat == null || muxing)) {
				int decoderOutputBufferIndex = videoDecoder.dequeueOutputBuffer(
					videoDecoderOutputBufferInfo, TIMEOUT_USEC);
				if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)
						Log.d(TAG, "no video decoder output buffer");
					break;
				}
				if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					if (VERBOSE)
						Log.d(TAG, "video decoder: output buffers changed");
					videoDecoderOutputBuffers = videoDecoder.getOutputBuffers();
					break;
				}
				if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					decoderOutputVideoFormat = videoDecoder.getOutputFormat();
					if (VERBOSE) {
						Log.d(TAG, "video decoder: output format changed: "
							+ decoderOutputVideoFormat);
					}
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "video decoder: returned output buffer: "
						+ decoderOutputBufferIndex);
					Log.d(TAG, "video decoder: returned buffer of size "
						+ videoDecoderOutputBufferInfo.size);
				}
				ByteBuffer decoderOutputBuffer = videoDecoderOutputBuffers[decoderOutputBufferIndex];
				if ((videoDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					if (VERBOSE)
						Log.d(TAG, "video decoder: codec config buffer");
					videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "video decoder: returned buffer for time "
						+ videoDecoderOutputBufferInfo.presentationTimeUs);
				}
				boolean render = videoDecoderOutputBufferInfo.size != 0;
				videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex, render);
				if (render) {
					if (VERBOSE)
						Log.d(TAG, "output surface: await new image");
					outputSurface.awaitNewImage();
					// Edit the frame and send it to the encoder.
					if (VERBOSE)
						Log.d(TAG, "output surface: draw image");
					outputSurface.drawImage();
					inputSurface.setPresentationTime(
						videoDecoderOutputBufferInfo.presentationTimeUs * 1000);
					if (VERBOSE)
						Log.d(TAG, "input surface: swap buffers");
					inputSurface.swapBuffers();
					if (VERBOSE)
						Log.d(TAG, "video encoder: notified of new frame");
				}
				if ((videoDecoderOutputBufferInfo.flags
					& MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (VERBOSE)
						Log.d(TAG, "video decoder: EOS");
					videoDecoderDone = true;
					videoEncoder.signalEndOfInputStream();
				}
				videoDecodedFrameCount++;
				// We extracted a pending frame, let's try something else next.
				break;
			}

			// Poll output frames from the audio decoder.
			// Do not poll if we already have a pending buffer to feed to the encoder.
			while (mCopyAudio && !audioDecoderDone && pendingAudioDecoderOutputBufferIndex == -1
				&& (encoderOutputAudioFormat == null || muxing)) {
				int decoderOutputBufferIndex = audioDecoder.dequeueOutputBuffer(
					audioDecoderOutputBufferInfo, TIMEOUT_USEC);
				if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)
						Log.d(TAG, "no audio decoder output buffer");
					break;
				}
				if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					if (VERBOSE)
						Log.d(TAG, "audio decoder: output buffers changed");
					audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
					break;
				}
				if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					decoderOutputAudioFormat = audioDecoder.getOutputFormat();
					if (VERBOSE) {
						Log.d(TAG, "audio decoder: output format changed: "
							+ decoderOutputAudioFormat);
					}
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: returned output buffer: "
						+ decoderOutputBufferIndex);
				}
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: returned buffer of size "
						+ audioDecoderOutputBufferInfo.size);
				}
				ByteBuffer decoderOutputBuffer = audioDecoderOutputBuffers[decoderOutputBufferIndex];
				if ((audioDecoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					if (VERBOSE)
						Log.d(TAG, "audio decoder: codec config buffer");
					audioDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: returned buffer for time "
						+ audioDecoderOutputBufferInfo.presentationTimeUs);
				}
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: output buffer is now pending: "
						+ pendingAudioDecoderOutputBufferIndex);
				}
				pendingAudioDecoderOutputBufferIndex = decoderOutputBufferIndex;
				audioDecodedFrameCount++;
				// We extracted a pending frame, let's try something else next.
				break;
			}

			// Feed the pending decoded audio buffer to the audio encoder.
			while (mCopyAudio && pendingAudioDecoderOutputBufferIndex != -1) {
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: attempting to process pending buffer: "
						+ pendingAudioDecoderOutputBufferIndex);
				}
				int encoderInputBufferIndex = audioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
				if (encoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
					if (VERBOSE)
						Log.d(TAG, "no audio encoder input buffer");
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "audio encoder: returned input buffer: " + encoderInputBufferIndex);
				}
				ByteBuffer encoderInputBuffer = audioEncoderInputBuffers[encoderInputBufferIndex];
				int size = audioDecoderOutputBufferInfo.size;
				long presentationTime = audioDecoderOutputBufferInfo.presentationTimeUs;
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: processing pending buffer: "
						+ pendingAudioDecoderOutputBufferIndex);
				}
				if (VERBOSE) {
					Log.d(TAG, "audio decoder: pending buffer of size " + size);
					Log.d(TAG, "audio decoder: pending buffer for time " + presentationTime);
				}
				if (size >= 0) {
					ByteBuffer decoderOutputBuffer = audioDecoderOutputBuffers[pendingAudioDecoderOutputBufferIndex]
						.duplicate();
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
					if (VERBOSE)
						Log.d(TAG, "audio decoder: EOS");
					audioDecoderDone = true;
				}
				// We enqueued a pending frame, let's try something else next.
				break;
			}

			// Poll frames from the video encoder and send them to the muxer.
			while (mCopyVideo && !videoEncoderDone
				&& (encoderOutputVideoFormat == null || muxing)) {
				int encoderOutputBufferIndex = videoEncoder.dequeueOutputBuffer(
					videoEncoderOutputBufferInfo, TIMEOUT_USEC);
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
					if (outputVideoTrack >= 0) {
						fail("video encoder changed its output format again?");
					}
					encoderOutputVideoFormat = videoEncoder.getOutputFormat();
					break;
				}
				assertTrue("should have added track before processing output", muxing);
				if (VERBOSE) {
					Log.d(TAG, "video encoder: returned output buffer: "
						+ encoderOutputBufferIndex);
					Log.d(TAG, "video encoder: returned buffer of size "
						+ videoEncoderOutputBufferInfo.size);
				}
				ByteBuffer encoderOutputBuffer = videoEncoderOutputBuffers[encoderOutputBufferIndex];
				if ((videoEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					if (VERBOSE)
						Log.d(TAG, "video encoder: codec config buffer");
					// Simply ignore codec config buffers.
					videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "video encoder: returned buffer for time "
						+ videoEncoderOutputBufferInfo.presentationTimeUs);
				}
				if (videoEncoderOutputBufferInfo.size != 0) {
					muxer.writeSampleData(
						outputVideoTrack, encoderOutputBuffer, videoEncoderOutputBufferInfo);
				}
				if ((videoEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (VERBOSE)
						Log.d(TAG, "video encoder: EOS");
					videoEncoderDone = true;
				}
				videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
				videoEncodedFrameCount++;
				// We enqueued an encoded frame, let's try something else next.
				break;
			}

			// Poll frames from the audio encoder and send them to the muxer.
			while (mCopyAudio && !audioEncoderDone
				&& (encoderOutputAudioFormat == null || muxing)) {
				int encoderOutputBufferIndex = audioEncoder.dequeueOutputBuffer(
					audioEncoderOutputBufferInfo, TIMEOUT_USEC);
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
						fail("audio encoder changed its output format again?");
					}

					encoderOutputAudioFormat = audioEncoder.getOutputFormat();
					break;
				}
				assertTrue("should have added track before processing output", muxing);
				if (VERBOSE) {
					Log.d(TAG, "audio encoder: returned output buffer: "
						+ encoderOutputBufferIndex);
					Log.d(TAG, "audio encoder: returned buffer of size "
						+ audioEncoderOutputBufferInfo.size);
				}
				ByteBuffer encoderOutputBuffer = audioEncoderOutputBuffers[encoderOutputBufferIndex];
				if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					if (VERBOSE)
						Log.d(TAG, "audio encoder: codec config buffer");
					// Simply ignore codec config buffers.
					audioEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
					break;
				}
				if (VERBOSE) {
					Log.d(TAG, "audio encoder: returned buffer for time "
						+ audioEncoderOutputBufferInfo.presentationTimeUs);
				}
				if (audioEncoderOutputBufferInfo.size != 0) {
					muxer.writeSampleData(
						outputAudioTrack, encoderOutputBuffer, audioEncoderOutputBufferInfo);
				}
				if ((audioEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (VERBOSE)
						Log.d(TAG, "audio encoder: EOS");
					audioEncoderDone = true;
				}
				audioEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
				audioEncodedFrameCount++;
				// We enqueued an encoded frame, let's try something else next.
				break;
			}

			if (!muxing
				&& (!mCopyAudio || encoderOutputAudioFormat != null)
				&& (!mCopyVideo || encoderOutputVideoFormat != null)) {
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
			assertEquals("encoded and decoded video frame counts should match",
				videoDecodedFrameCount, videoEncodedFrameCount);
			assertTrue("decoded frame count should be less than extracted frame count",
				videoDecodedFrameCount <= videoExtractedFrameCount);
		}
		if (mCopyAudio) {
			assertEquals("no frame should be pending", -1, pendingAudioDecoderOutputBufferIndex);
		}

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
