package com.yy.lvf.ctm;

import java.util.List;
import java.util.ListIterator;

public class Session {
	private String		type;
	private int			startTimestamp;
	private int			endTimestamp;
	private int			currentSessionEndTime;
	private List<Talk>	talkList;

	public String getType() {
		return type;
	}

	public void setType(String type) {
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

	public int getCurrentSessionEndTime() {
		return currentSessionEndTime;
	}

	public void setCurrentSessionEndTime(int currentSessionEndTime) {
		this.currentSessionEndTime = currentSessionEndTime;
	}

	public int calcEndSessionTime() {
		int tsum = 0;
		for (Talk i : talkList) {
			tsum += i.getDuration();
		}
		currentSessionEndTime = (tsum + startTimestamp);
		return currentSessionEndTime;
	}

	public void print(int startTimestamp) {
		int currentTime = this.startTime;
		for (Talk talk : talks) {
			String s = formatTime(currentTime);
			talk.print(s);
			// JSON binding may follow here for JSON output
			currentTime += talk.getTimeDuration();
		}
	}

	public void schedule(List<Talk> validTalksList) throws SchedulerException {
		KnapSackSolverRequest req = setKnapSackSolverRequest(validTalksList);
		KnapSackSolverResponse res = new KnapSackSolverResponse();
		useSolver(req, res);
		updateValidTalkList(validTalksList, res);
		return;
	}

	private void useSolver(KnapSackSolverRequest req, KnapSackSolverResponse res) {
		KnapSackSolver knapSolver = new KnapSackSolver();
		try {
			knapSolver.solver(req, res);
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private KnapSackSolverRequest setKnapSackSolverRequest(List<Talk> validTalksList) {
		int W = this.getEndTime() - this.getStartTime();
		int N = validTalksList.size();

		int[] profit = new int[N + 1];
		int[] weight = new int[N + 1];
		int i = 1;
		// profit and weight are same in this case
		for (Talk proft : validTalksList) {
			profit[i] = weight[i] = proft.getTimeDuration();
			i++;
		}
		KnapSackSolverRequest req = new KnapSackSolverRequest();
		req.setMaxKnapSackSize(W);
		req.setNumSize(N);
		req.setProfit(profit);
		req.setWeight(weight);
		return req;
	}

	private void updateValidTalkList(List<Talk> validTalksList, KnapSackSolverResponse res) {
		int i;
		boolean[] take;
		take = res.getTake();
		i = 1;
		for (ListIterator<Talk> iter = validTalksList.listIterator(); iter.hasNext();) {
			Talk talk = iter.next();
			if (take[i]) {
				talk.setIncluded(true);
				this.addTalk(talk);
				iter.remove();
			}
			i++;
		}
	}
}
