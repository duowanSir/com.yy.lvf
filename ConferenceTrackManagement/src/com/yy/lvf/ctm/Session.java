package com.yy.lvf.ctm;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/*
 * 对会议的抽象**/
public class Session {
	private SessionType		type;
	private int				startTimestamp;			// 会议开始时间
	private int				endTimestamp;			// 会议结束时间
	private int				lastTalkEndTimestamp;	// 会议最后一个会谈结束时间,用来确定Networking Event的开始时间;
	private List<Talk>		talks;

	private PacksackParam	packsackParam;

	public SessionType getType() {
		return type;
	}

	public void setType(SessionType type) {
		this.type = type;
	}

	public int getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(int startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public int getEndTimestamp() {
		return endTimestamp;
	}

	public void setEndTimestamp(int endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	public int getLastTalkEndTimestamp() {
		return lastTalkEndTimestamp;
	}

	public void setLastTalkEndTimestamp(int lastTalkEndTimestamp) {
		this.lastTalkEndTimestamp = lastTalkEndTimestamp;
	}

	public int calcLastTalkEndTimestamp() {
		int tsum = 0;
		for (Talk i : talks) {
			tsum += i.getDuration();
		}
		lastTalkEndTimestamp = (tsum + startTimestamp);
		return lastTalkEndTimestamp;
	}

	public void print() {
		int currentTime = startTimestamp;
		for (Talk talk : talks) {
			String s = formatTimestamp(currentTime);
			talk.print(s);
			currentTime += talk.getDuration();
		}
		if (type == SessionType.AM) {
			System.out.println("12:00 PM Lunch");
		} else if (type == SessionType.PM) {
			System.out.println(formatTimestamp(lastTalkEndTimestamp) + " Networking Event");
		}
	}

	/**
	 * @param timestamp
	 *            时刻,单位分钟;
	 */
	private String formatTimestamp(int timestamp) {
		int hour = timestamp / 60;
		String dTime;
		if (hour < 12)
			dTime = SessionType.AM.getSessionType();
		else
			dTime = SessionType.PM.getSessionType();
		if (hour > 12)
			hour = hour - 12;
		return String.format("%02d:%02d %s", hour, (timestamp % 60), dTime);
	}

	public void fillSessionWithTalk(List<Talk> validTalksList) {
		transformToPacksackParam(validTalksList);
		boolean[] taken = DPForPacksackProcessor.packsackDp(packsackParam);
		updateValidTalkList(validTalksList, taken);
		calcLastTalkEndTimestamp();
	}

	private void transformToPacksackParam(List<Talk> validTalksList) {
		packsackParam = new PacksackParam();
		packsackParam.setPacksackWeight(endTimestamp - startTimestamp);
		packsackParam.setGoodsNumber(validTalksList.size());
		int[] profit = new int[packsackParam.getGoodsNumber() + 1];
		int[] weight = new int[packsackParam.getGoodsNumber() + 1];
		for (int i = 0; i < validTalksList.size(); i++) {
			Talk j = validTalksList.get(i);
			profit[i + 1] = weight[i + 1] = j.getDuration();
		}
		packsackParam.setGoodsProfitArr(profit);
		packsackParam.setGoodsWeightArr(weight);
	}

	private void updateValidTalkList(List<Talk> validTalksList, boolean[] taken) {
		int i = 1;
		for (ListIterator<Talk> iter = validTalksList.listIterator(); iter.hasNext();) {
			Talk talk = iter.next();
			if (taken[i]) {
				if (talks == null) {
					talks = new ArrayList<>();
				}
				talks.add(talk);
				iter.remove();
			}
			i++;
		}
	}

	public enum SessionType {
		AM("AM"), PM("PM");

		private String sessionType;

		private SessionType(String sessionType) {
			this.sessionType = sessionType;
		}

		public String getSessionType() {
			return sessionType;
		}

		public void setSessionType(String sessionType) {
			this.sessionType = sessionType;
		}

	}

	public static class PacksackParam {
		private int		packsackWeight;	// 背包容量权值
		private int		goodsNumber;	// 物品个数
		private int[]	goodsWeightArr;	//物品权值
		private int[]	goodsProfitArr;	//物品价值

		public int getPacksackWeight() {
			return packsackWeight;
		}

		public void setPacksackWeight(int packsackWeight) {
			this.packsackWeight = packsackWeight;
		}

		public int getGoodsNumber() {
			return goodsNumber;
		}

		public void setGoodsNumber(int goodsNumber) {
			this.goodsNumber = goodsNumber;
		}

		public int[] getGoodsWeightArr() {
			return goodsWeightArr;
		}

		public void setGoodsWeightArr(int[] goodsWeightArr) {
			this.goodsWeightArr = goodsWeightArr;
		}

		public int[] getGoodsProfitArr() {
			return goodsProfitArr;
		}

		public void setGoodsProfitArr(int[] goodsProfitArr) {
			this.goodsProfitArr = goodsProfitArr;
		}

	}
}
