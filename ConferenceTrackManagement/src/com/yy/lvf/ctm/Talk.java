package com.yy.lvf.ctm;

/**
 * Talk对象,对(Sit Down and Write 30min)的抽象
 */
public class Talk {
	private String	lineStr;	// 原始数据,用于打印;
	private int		duration;	// 持续时间

	public Talk() {
	}

	public Talk(String lineStr, int durationMin) {
		this.lineStr = lineStr;
		this.duration = durationMin;
	}

	public String getLineStr() {
		return lineStr;
	}

	public void setLineStr(String lineStr) {
		this.lineStr = lineStr;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int durationMin) {
		this.duration = durationMin;
	}

}
