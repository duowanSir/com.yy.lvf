package com.yy.lvf.myegl;

import java.io.File;

import com.yy.lvf.LLog;

import android.opengl.EGL14;
import android.opengl.EGLSurface;

public class EglSurfaceBase {
	public static final String	TAG			= EglSurfaceBase.class.getSimpleName();
	private EglCore				mEglCore;
	private EGLSurface			mEglSurface	= EGL14.EGL_NO_SURFACE;
	private int					mWidth		= -1;
	private int					mHeight		= -1;

	protected EglSurfaceBase(EglCore eglCore) {
		mEglCore = eglCore;
	}

	public void createWindowSurface(Object surface) {
		if (mEglSurface != EGL14.EGL_NO_SURFACE) {
			throw new IllegalStateException("surface has already inited");
		}
		mEglSurface = mEglCore.createWindowSurface(surface);
	}

	public void createOffScreenSurface(int width, int height) {
		if (mEglSurface != EGL14.EGL_NO_SURFACE) {
			throw new IllegalStateException("surface has already inited");
		}
		mEglSurface = mEglCore.createOffScreenSurface(width, height);
		mWidth = width;
		mHeight = height;
	}

	public int getWidth() {
		if (mWidth < 0) {
			return mEglCore.querySurface(mEglSurface, EGL14.EGL_WIDTH);
		} else {
			return mWidth;
		}
	}

	public int getHeight() {
		if (mHeight < 0) {
			return mEglCore.querySurface(mEglSurface, EGL14.EGL_HEIGHT);
		} else {
			return mHeight;
		}
	}

	public void makeCurrent() {
		mEglCore.makeCurrent(mEglSurface);
	}

	public void swapBuffers() {
		mEglCore.swapBuffers(mEglSurface);
	}

	public void setPresentationTime(long time) {
		mEglCore.setPresentationTime(mEglSurface, time);
	}

	public void saveFrame(File dstFile) {
		if (!mEglCore.isCurrent(mEglSurface)) {
			LLog.d(TAG, "eglCore and eglSurface is not current to current thread");
			return;
		}
	}

	public void releaseEglSurface() {
		mEglCore.releaseSurface(mEglSurface);
		mEglSurface = EGL14.EGL_NO_SURFACE;
		mWidth = mHeight = -1;
	}
}
