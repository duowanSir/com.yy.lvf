package com.yy.lvf.ctm;

/**
 * 会谈对象,对例如(Sit Down and Write 30min)的抽象
 */
public class Talk {
	private String	input;		// 原始数据,用于打印;
	private int		duration;	// 持续时间

	public Talk(String input, int durationMin) {
		this.input = input;
		this.duration = durationMin;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int durationMin) {
		this.duration = durationMin;
	}

	public void print(Object talkStartTimestamp) {
		if (talkStartTimestamp == null) {
			return;
		}
		System.out.println("" + talkStartTimestamp + " " + input);
	}

}
