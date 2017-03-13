package com.yy.lvf.ctm;

import java.util.List;

public class CtmParamHelper {
	private String inputfilepath;//待处理文件路径
	private String minuteSuffix;//分后缀
	private String lightningSuffix;//简会后缀
	private int maxTalkMinutes;//最大会谈时长
	private int minTalkMinutes;//最短会谈时长
	private int lightningMinutes;//简会时长
	private int minNetworkingStartTime;//最短网络开始时长
	private List<SessionParam> sessionlist;//会话参数
	
	public String getInputfilepath() {
		return inputfilepath;
	}

	public void setInputfilepath(String inputfilepath) {
		this.inputfilepath = inputfilepath;
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

	public List<SessionParam> getSessionlist() {
		return sessionlist;
	}

	public void setSessionlist(List<SessionParam> sessionlist) {
		this.sessionlist = sessionlist;
	}
	
	private void init(){
		
	}

	public static CtmParamHelper getInstance(){
		return INSTANCE.INSTANCE;
	}
	private static class INSTANCE{
		private static final CtmParamHelper INSTANCE=new CtmParamHelper();
	}
	
	public static class SessionParam{
		int id;
		String name;
		int startTime;
		int endTime;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
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
