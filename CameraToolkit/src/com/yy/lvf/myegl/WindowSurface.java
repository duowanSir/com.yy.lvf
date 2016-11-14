package com.yy.lvf.myegl;

import android.graphics.SurfaceTexture;
import android.view.Surface;

public class WindowSurface extends EglSurfaceBase {
	private Surface	mSurface;
	private boolean	mReleaseSurface;

	public WindowSurface(EglCore eglCore, Surface surface, boolean releaseSurface) {
		super(eglCore);
		createWindowSurface(surface);
		mSurface = surface;
		mReleaseSurface = releaseSurface;
	}
	
	public WindowSurface(EglCore eglCore, SurfaceTexture surfaceTexture) {
		super(eglCore);
		createWindowSurface(surfaceTexture);
	}
	
	public void release() {
		releaseEglSurface();
		if (mSurface != null) {
			if (mReleaseSurface) {
				mSurface.release();
			}
		}
		mSurface = null;
	}

}
