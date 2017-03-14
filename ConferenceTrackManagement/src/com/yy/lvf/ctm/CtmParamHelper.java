package com.yy.lvf.ctm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.yy.lvf.ctm.Session.SessionType;

public class CtmParamHelper {
	private String				inputFilePath;			//待处理文件路径
	private String				minuteSuffix;			//分后缀
	private String				lightningSuffix;		//简报后缀
	private int					maxTalkMinutes;			//最大会谈时长
	private int					minTalkMinutes;			//最短会谈时长
	private int					lightningMinutes;		//简会时长
	private int					minNetworkingStartTime;	//最短网络开始时长
	private List<SessionParam>	sessionParams;			//会话参数

	private CtmParamHelper() {
		/*
		 * 默认参数**/
		StringBuilder sb = new StringBuilder();
		sb.append("res").append(File.separator).append("input.txt");
		inputFilePath = sb.toString();
		minuteSuffix = "min";
		lightningSuffix = "lightning";
		maxTalkMinutes = 180;
		minTalkMinutes = 1;
		lightningMinutes = 5;
		minNetworkingStartTime = 1600;
		sessionParams = new ArrayList<>();
		SessionParam sp = new SessionParam(1, SessionType.AM, 540, 720);
		sessionParams.add(sp);
		sp = new SessionParam(1, SessionType.PM, 780, 1020);
		sessionParams.add(sp);
	}

	public String getInputFilePath() {
		return inputFilePath;
	}

	public void setInputFilePath(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}

	public String getMinuteSuffix() {
		return minuteSuffix;
	}

	public void setMinuteSuffix(String minuteSuffix) {
		this.minuteSuffix = minuteSuffix;
	}

	public String getLightningSuffix() {
		return lightningSuffix;
	}

	public void setLightningSuffix(String lightningSuffix) {
		this.lightningSuffix = lightningSuffix;
	}

	public int getMaxTalkMinutes() {
		return maxTalkMinutes;
	}

	public void setMaxTalkMinutes(int maxTalkMinutes) {
		this.maxTalkMinutes = maxTalkMinutes;
	}

	public int getMinTalkMinutes() {
		return minTalkMinutes;
	}

	public void setMinTalkMinutes(int minTalkMinutes) {
		this.minTalkMinutes = minTalkMinutes;
	}

	public int getLightningMinutes() {
		return lightningMinutes;
	}

	public void setLightningMinutes(int lightningMinutes) {
		this.lightningMinutes = lightningMinutes;
	}

	public int getMinNetworkingStartTime() {
		return minNetworkingStartTime;
	}

	public void setMinNetworkingStartTime(int minNetworkingStartTime) {
		this.minNetworkingStartTime = minNetworkingStartTime;
	}

	public List<SessionParam> getSessionParams() {
		return sessionParams;
	}

	public void setSessionParams(List<SessionParam> sessionParams) {
		this.sessionParams = sessionParams;
	}

	public void init() {

	}

	public static CtmParamHelper getInstance() {
		return INSTANCE.INSTANCE;
	}

	private static class INSTANCE {
		private static final CtmParamHelper INSTANCE = new CtmParamHelper();
	}

	public static class SessionParam {
		int			id;
		SessionType	type;
		int			startTime;
		int			endTime;

		public SessionParam(int id, SessionType type, int startTime, int endTime) {
			this.id = id;
			this.type = type;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public SessionType getType() {
			return type;
		}

		public void setType(SessionType type) {
			this.type = type;
		}

		public int getStartTime() {
			return startTime;
		}

		public void setStartTime(int startTime) {
			this.startTime = startTime;
		}

		public int getEndTime() {
			return endTime;
		}

		public void setEndTime(int endTime) {
			this.endTime = endTime;
		}
	}

}
