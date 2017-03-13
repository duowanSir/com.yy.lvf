package com.yy.lvf.ctm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.yy.lvf.ctm.CtmParamHelper.SessionParam;

public class Main {

	public static void main(String[] args) {
		String inputfile = null;
		if (args.length > 0) {
			inputfile = args[0];
		}
		List<String> inputStrList = IOHelper.readInput(inputfile);
		List<Track> trackList = getTrackList(inputStrList);

		System.out.println("\nInput : \n");
		for (String s : inputStrList) {
			System.out.println(s);
		}
		System.out.println("\nOutput : \n");
		for (Track i : trackList) {
			System.out.println("Track : " + i.getID() + "\n");
			i.print();
		}
	}

	private static List<Track> getTrackList(List<String> inviteeList) {
		List<Track> trackList = new ArrayList<Track>();
		try {
			List<Talk> talkList = IOHelper.generateValidTalks(inviteeList);
			List<SessionParam> sessionlist = CtmParamHelper.getInstance().getSessionlist();
			while (talkList.size() > 0) {// 有多余的Talk就往Session里面装
				Iterator<SessionParam> iterator = sessionlist.iterator();
				Track track = new Track();
				trackList.add(track);
				while (iterator.hasNext()) {// Talk总是由AM Session往PM Session放
					SessionParam SessionParam = (SessionParam) iterator.next();
					Session session = new Session();
					track.addNewSession(session);
					if (talkList.size() > 0)
						session.schedule(talkList);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trackList;
	}

}
