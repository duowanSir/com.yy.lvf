package com.yy.lvf;

import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.view.Surface;

public class CameraUtil {
	public static class CameraInstanceAndId {
		public Camera	mCamera;
		public int		mCameraId;

		public CameraInstanceAndId(Camera camera, int id) {
			mCamera = camera;
			mCameraId = id;
		}
	}

	public static final String TAG = CameraUtil.class.getSimpleName();

	public static CameraInstanceAndId openCamera(int type) {
		int cameraNum = Camera.getNumberOfCameras();
		for (int i = 0; i < cameraNum; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == type) {
				Camera camera = Camera.open(i);
				CameraInstanceAndId cii = new CameraInstanceAndId(camera, i);
				return cii;
			}
		}
		return null;
	}

	public static Size selectPreviewSize(int desiredWidth, int desiredHeight, List<Size> supportedSize, int displayOrientation) {
		if (displayOrientation == 90 || displayOrientation == 270) {
			int m = desiredWidth;
			desiredWidth = desiredHeight;
			desiredHeight = m;
		}
		int j = -1;
		int minOffset = Integer.MAX_VALUE;
		float desiredRatio = (float) desiredHeight / desiredWidth;
		float resultRatio = Float.MAX_VALUE;
		for (int i = 0; i < supportedSize.size(); i++) {
			Size s = supportedSize.get(i);
			if (s.width == desiredWidth) {
				if (s.height == desiredHeight) {
					return s;
				} else {
					int offset = Math.abs(s.height - desiredHeight);
					if (offset < minOffset) {
						j = i;
						minOffset = offset;
					} else if (offset == minOffset) {
						Size sj = supportedSize.get(j);
						if (sj.height > s.height) {
							j = i;
						}
					}
				}
				continue;
			}
			float ratio = (float) s.height / s.width;
			if (ratio >= desiredRatio && ratio <= resultRatio) {
				int offset = Math.abs(s.width - desiredWidth);
				if (offset < minOffset) {
					j = i;
					minOffset = offset;
				} else if (offset == minOffset) {
					Size sj = supportedSize.get(j);
					if (sj.width > s.width) {
						j = i;
					}
				}
			}
		}
		if (j == -1) {
			return null;
		} else {
			return supportedSize.get(j);
		}
	}

	public static int selectDisplayOrientation(int facing, int cameraOrientation, int screenOritation) {
		switch (screenOritation) {
		case Surface.ROTATION_0:
			screenOritation = 0;
			break;
		case Surface.ROTATION_90:
			screenOritation = 90;
			break;
		case Surface.ROTATION_180:
			screenOritation = 180;
			break;
		case Surface.ROTATION_270:
			screenOritation = 270;
			break;
		default:
			throw new IllegalArgumentException("unsupport screenOrientation " + screenOritation);
		}
		int cameraDisplayOrientation = 0;
		if (facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			cameraDisplayOrientation = (cameraOrientation + screenOritation) % 360;
			cameraDisplayOrientation = (360 - cameraDisplayOrientation) % 360;
		} else {
			cameraDisplayOrientation = (cameraOrientation - screenOritation + 360) % 360;
		}
		return cameraDisplayOrientation;
	}

	public static int selectFixedFps(Camera.Parameters parms, int desiredThousandFps) {
		List<int[]> supported = parms.getSupportedPreviewFpsRange();
		for (int[] entry : supported) {
			if ((entry[0] == entry[1]) && (entry[0] == desiredThousandFps)) {
				parms.setPreviewFpsRange(entry[0], entry[1]);
				return entry[0];
			}
		}

		int[] tmp = new int[2];
		parms.getPreviewFpsRange(tmp);
		int guess;
		if (tmp[0] == tmp[1]) {
			guess = tmp[0];
		} else {
			guess = tmp[1] / 2;
		}

		LLog.d(TAG, "Couldn't find match for " + desiredThousandFps + ", using " + guess);
		return guess;
	}
}
