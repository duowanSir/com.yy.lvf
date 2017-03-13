package com.yy.lvf.ctm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Track {
	static int				TRACK_ID_INCREMENT	= 1;
	private final int		id;
	private List<Session>	sessionList;

	public int getID() {
		return id;
	}

	public Track() {
		id = TRACK_ID_INCREMENT++;
		sessionList = new ArrayList<Session>();
	}

	public void addNewSession(Session s) {
		sessionList.add(s);
	}

	public void print() {
		Iterator<Session> iter = sessionList.iterator();
		int currentSessionEndTime = 0, prevSessionEndTime = 0;
		while (iter.hasNext()) {
			Session s = iter.next();
			currentSessionEndTime = s.calcEndSessionTime();
			s.print(prevSessionEndTime);
			prevSessionEndTime = currentSessionEndTime;
		}
	}
}
