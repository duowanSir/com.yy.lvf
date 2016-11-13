package com.yy.lvf.mygles;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.util.Log;

import com.yy.lvf.LLog;

public class EglCore {
	public static final String	TAG						= EglCore.class.getSimpleName();
	public static final int		FLAG_RECORDABLE			= 0x01;
	public static final int		FLAG_TRY_GLES3			= 0x02;
	public static final int		EGL_RECORDABLE_ANDROID	= 0x3142;

	private EGLDisplay			mEglDisplay;
	private EGLConfig			mEglConfig;
	private EGLContext			mEglContext;
	private int					mVersion;

	public EglCore(EGLContext sharedEglContext, int flag) {
		if (mEglDisplay != null) {
			throw new IllegalStateException("eglDisplay is not null");
		}
		mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
		if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
			throw new IllegalStateException("get eglDisplay failed");
		}
		int[] mm = new int[2];
		if (!EGL14.eglInitialize(mEglDisplay, mm, 0, mm, 1)) {
			mEglDisplay = null;
			throw new IllegalStateException("init eglDisplay failed");
		}
		LLog.d(TAG, "eglVersion[" + mm[0] + ", " + mm[1] + "]");
		if (sharedEglContext == null) {
			sharedEglContext = EGL14.EGL_NO_CONTEXT;
		}
		if ((flag & FLAG_TRY_GLES3) != 0) {
			EGLConfig eglConfig = getConfig(flag, 3);
			if (eglConfig != null) {
				int[] attribs = new int[] { EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE };
				// 生成的context和sharedEglContext共享数据,并且会和与sharedEglContext共享数据的任何context共享数据.
				EGLContext eglContext = EGL14.eglCreateContext(mEglDisplay, eglConfig, sharedEglContext, attribs, 0);
				if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
					mEglConfig = eglConfig;
					mEglContext = eglContext;
					mVersion = 3;
				}
			}
		}
		if (mEglContext == EGL14.EGL_NO_CONTEXT) {
			EGLConfig eglConfig = getConfig(flag, 2);
			if (eglConfig == null) {
				throw new IllegalStateException("init eglConfig failed");
			}
			int[] attribs = new int[] { EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE };
			EGLContext eglContext = EGL14.eglCreateContext(mEglDisplay, eglConfig, sharedEglContext, attribs, 0);
			if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
				mEglConfig = eglConfig;
				mEglContext = eglContext;
				mVersion = 2;
			} else {
				throw new IllegalStateException("init eglContext failed");
			}
			int[] attribValues = new int[1];
			// EGL14.EGL_CONTEXT_CLIENT_VERSION是其中一个关于context的可查属性
			EGL14.eglQueryContext(mEglDisplay, mEglContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, attribValues, 0);
			LLog.d(TAG, "eglVersion" + attribValues[0]);
		}
	}

	private EGLConfig getConfig(int flag, int version) {
		int renderableType = EGL14.EGL_OPENGL_ES2_BIT;
		if (version >= 3) {
			renderableType = EGLExt.EGL_OPENGL_ES3_BIT_KHR;
		}
		int[] attribs = new int[] { EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8, EGL14.EGL_RENDERABLE_TYPE, renderableType, EGL14.EGL_NONE, 0, EGL14.EGL_NONE };
		if ((flag & FLAG_RECORDABLE) != 0) {
			attribs[attribs.length - 3] = EGL_RECORDABLE_ANDROID;
			attribs[attribs.length - 2] = 1;
		}
		EGLConfig[] configs = new EGLConfig[1];
		int[] configNum = new int[1];
		// 返回符合attribs的egl frame buffer的配置列表
		// configNum中返回所有符合的配置个数
		if (!EGL14.eglChooseConfig(mEglDisplay, attribs, 0, configs, 0, configs.length, configNum, 0)) {
			return null;
		}
		return configs[0];
	}

	public void release() {
		if (mEglDisplay != null) {
			// 将context和当前渲染线程,输入,输出surface关联起来,后续所有gl操作都是对输入,输出surface的操作.
			// 如果当前线程已经关联过,那么之前关联过的将失效.
			// context第一次被makeCurrent时,viewPort和scissor dimension就被赋值并固定,后续绑定不会影响这两个参数
			// 解绑定操作如下:
			EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
			// 如果context已经解绑定,那么应当立即销毁它.
			EGL14.eglDestroyContext(mEglDisplay, mEglContext);
			// 释放和eglDisplay连接关联的所有资源,当和eglDisplay关联的context或surface对某个线程是当前的,那么该函数不起作用.
			// eglTerminate和eglInitialize对应
			EGL14.eglTerminate(mEglDisplay);
		}
		mEglContext = EGL14.EGL_NO_CONTEXT;
		mEglConfig = null;
		mEglDisplay = EGL14.EGL_NO_DISPLAY;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (mEglDisplay != EGL14.EGL_NO_DISPLAY) {
				release();
			}
		} finally {
			super.finalize();
		}
	}

	public void releaseSurface(EGLSurface eglSurface) {
		// 如果eglSurface对任何线程都不是当前的,那么请立即调用销毁.
		// 另外只有当和texture绑定的pBuffer surface的colorBuffer都释放了,该方法才对pBuffer有效.
		EGL14.eglDestroySurface(mEglDisplay, eglSurface);
	}

	public EGLSurface createWindowSurface(Object surface) {
		// 参数2要么是能获取surface的对象,要么是surfaceTexture.
		// 按照参数1创建的eglContext才能渲染到该API定义的surface中.
		// 使用makeCurrent绑定eglContext到该surface上.
		// windowSurface=native window
		// pixmapSurface=native pixmap
		// pixbufferSurface!=native pixbufferSurface
		// 如果创建的surface要给MediaCodec使用,则参数1中必须包含EGL_RENDERABLE_TYPE属性.
		EGLSurface eglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, null, 0);
		if (EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
			throw new IllegalStateException("create window surface failed");
		}
		return eglSurface;
	}

	public EGLSurface createOffScreenSurface(int width, int height) {
		int[] attribs = new int[] { EGL14.EGL_WIDTH, width, EGL14.EGL_HEIGHT, height, EGL14.EGL_NONE };
		EGLSurface eglSurface = EGL14.eglCreatePbufferSurface(mEglDisplay, mEglConfig, attribs, 0);
		if (EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
			throw new IllegalStateException("create off screen surface failed");
		}
		return eglSurface;
	}

	public void makeCurrent(EGLSurface eglSurface) {
		if (!EGL14.eglMakeCurrent(mEglDisplay, eglSurface, eglSurface, mEglContext)) {
			throw new RuntimeException("eglMakeCurrent failed");
		}
	}

	public void makeCurrent(EGLSurface drawSurface, EGLSurface readSurface) {
		if (!EGL14.eglMakeCurrent(mEglDisplay, drawSurface, readSurface, mEglContext)) {
			throw new RuntimeException("eglMakeCurrent(draw,read) failed");
		}
	}

	public void makeNothingCurrent() {
		if (!EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
			throw new RuntimeException("eglMakeCurrent failed");
		}
	}

	public boolean swapBuffers(EGLSurface eglSurface) {
		// 调用后eglSurface的数据是保留还是销毁取决于EGL_SWAP_BEHAVIOR的值,可以通过eglSurfaceAttrib函数设置.
		// 该函数隐式的对context做了flush操作,该函数之后立即调用其他函数可能会出问题,不过该函数会阻塞.
		return EGL14.eglSwapBuffers(mEglDisplay, eglSurface);
	}

	public void setPresentationTime(EGLSurface eglSurface, long nsecs) {
		EGLExt.eglPresentationTimeANDROID(mEglDisplay, eglSurface, nsecs);
	}

	public boolean isCurrent(EGLSurface eglSurface) {
		return mEglContext.equals(EGL14.eglGetCurrentContext()) && eglSurface.equals(EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW));
	}

	public int querySurface(EGLSurface eglSurface, int what) {
		int[] value = new int[1];
		EGL14.eglQuerySurface(mEglDisplay, eglSurface, what, value, 0);
		return value[0];
	}

	public String queryString(int what) {
		return EGL14.eglQueryString(mEglDisplay, what);
	}

	public static void logCurrent(String msg) {
		EGLDisplay display;
		EGLContext context;
		EGLSurface surface;

		display = EGL14.eglGetCurrentDisplay();
		context = EGL14.eglGetCurrentContext();
		surface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
		Log.i(TAG, "Current EGL (" + msg + "): display=" + display + ", context=" + context + ", surface=" + surface);
	}

	public int getVersion() {
		return mVersion;
	}

}
