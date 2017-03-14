package com.yy.lvf.ctm;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.yy.lvf.ctm.Session.SessionType;

public class CtmParamHelper {
	private String				inputFilePath;		//待处理文件路径
	private String				minuteSuffix;		//分后缀
	private String				lightningSuffix;	//简报后缀
	private int					maxTalkMinutes;		//最大会谈时长
	private int					minTalkMinutes;		//最短会谈时长
	private int					lightningMinutes;	//简会时长
	private List<SessionParam>	sessionParams;		//会话参数

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

	public List<SessionParam> getSessionParams() {
		return sessionParams;
	}

	public void setSessionParams(List<SessionParam> sessionParams) {
		this.sessionParams = sessionParams;
	}

	public void init() {
		String properties = IOHelper.readSessionPacksackProperties();
		if (properties == null || properties.trim().length() <=0)
			return;
		Gson g = new Gson();
		try {
			CtmParamHelper param = g.fromJson(properties, CtmParamHelper.class);
			inputFilePath = param.inputFilePath;
			minuteSuffix = param.minuteSuffix;
			lightningSuffix = param.lightningSuffix;
			maxTalkMinutes = param.maxTalkMinutes;
			minTalkMinutes = param.minTalkMinutes;
			lightningMinutes = param.lightningMinutes;
			sessionParams = param.sessionParams;
			Iterator<SessionParam> iterator = param.sessionParams.iterator();
			while (iterator.hasNext()) {
				CtmParamHelper.SessionParam sessionParam = (CtmParamHelper.SessionParam) iterator.next();
				if (sessionParam == null) {
					iterator.remove();
					continue;
				}
				sessionParam.setTypeStr(sessionParam.getTypeStr());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		String		typeStr;
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

		public String getTypeStr() {
			return typeStr;
		}

		public void setTypeStr(String typeStr) throws IllegalArgumentException {
			this.typeStr = typeStr;
			if (typeStr == null) {
				throw new IllegalArgumentException("会议类型不能为空");
			}
			typeStr = typeStr.toUpperCase();
			if (SessionType.AM.getSessionType().equals(typeStr)) {
				type = SessionType.AM;
			} else if (SessionType.PM.getSessionType().equals(typeStr)) {
				type = SessionType.PM;
			} else {
				throw new IllegalArgumentException("会议类型和SessionType定义的不相符");
			}
		}
		
	}

}
