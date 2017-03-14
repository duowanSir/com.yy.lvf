package com.yy.lvf.ctm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Track {
	static int				TRACK_ID_INCREMENT	= 1;
	private final int		id;
	private List<Session>	sessions;

	public int getID() {
		return id;
	}

	public Track() {
		id = TRACK_ID_INCREMENT++;
		sessions = new ArrayList<Session>();
	}

	public void addNewSession(Session s) {
		sessions.add(s);
	}

	public void print() {
		System.out.println("Track " + id + ":");
		Iterator<Session> iterator = sessions.iterator();
		while (iterator.hasNext()) {
			Session s = iterator.next();
			s.print();
		}
	}
}
