package com.yy.lvf.mygles;

import com.yy.lvf.LLog;

import android.opengl.GLES20;

public class GlesUtil {
	public static final String TAG = GlesUtil.class.getSimpleName();
	
	// vertexShader控制展示形状,fragmentShader控制展示滤镜.
	public static int createProgram(String vertexSource, String fragmentSource) {
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
		if (vertexShader <= 0) {
			return 0;
		}
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		if (fragmentShader <= 0) {
			return 0;
		}
		int program = GLES20.glCreateProgram();
		checkError("glCreateProgram");
		if (program <= 0) {
			LLog.e(TAG, "create program failed");
			return 0;
		}
		GLES20.glAttachShader(program, vertexShader);
		checkError("glAttachShader(" + program + ", " + vertexShader + ")");
		GLES20.glAttachShader(program, fragmentShader);
		checkError("glAttachShader(" + program + ", " + fragmentShader + ")");
		// 连接成可执行单元,vertexShader运行在vertex处理器上,fragmentShader运行在fragment处理器上.
		// 通过glGetProgramiv来查询指定状态的值
		GLES20.glLinkProgram(program);
		int[] statusValues = new int[1];
		GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, statusValues, 0);
		if (statusValues[0] == GLES20.GL_FALSE) {
			LLog.e(TAG, "link program failed");
			return 0;
		}
		return program;
	}

	public static int loadShader(int shaderType, String source) {
		int shader = GLES20.glCreateShader(shaderType);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		int[] compiled = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == GLES20.GL_FALSE) {
			LLog.e(TAG, "Could not compile shader " + shaderType + ":");
			LLog.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			shader = 0;
		}
		return shader;
	}

	public static void checkError(String api) {
		int error = GLES20.glGetError();
		if (error != GLES20.GL_NO_ERROR) {
			throw new IllegalStateException(api + " failed");
		}
	}
	
	public static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }
}
