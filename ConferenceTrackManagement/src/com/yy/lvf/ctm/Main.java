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
		CtmParamHelper.getInstance().init();
		List<String> inputStrList = IOHelper.readInput(inputfile);

		System.out.println("\nInput :");
		for (String s : inputStrList) {
			System.out.println(s);
		}
		List<Track> trackList = getTrackList(inputStrList);
		System.out.println("\nOutput :");
		if(trackList == null || trackList.isEmpty()) {
			System.out.println("\n没有求解出任何Track");
			return;
		}
		for (Track i : trackList) {
			i.print();
		}
	}

	private static List<Track> getTrackList(List<String> input) {
		if (input == null || input.isEmpty()) {
			System.out.println("\n输入数据可能格式非法");
			return null;
		}
		List<Track> trackList = new ArrayList<Track>();
		try {
			List<Talk> talkList = IOHelper.generateValidTalks(input);
			List<SessionParam> sessionlist = CtmParamHelper.getInstance().getSessionParams();
			while (talkList.size() > 0) {// 有多余的Talk就往Session里面装
				Iterator<SessionParam> iterator = sessionlist.iterator();
				Track track = new Track();
				trackList.add(track);
				while (iterator.hasNext() && talkList.size() > 0) {// Talk总是由AM Session往PM Session放
					SessionParam sessionParam = (SessionParam) iterator.next();
					Session session = new Session();
					session.setType(sessionParam.getType());
					session.setStartTimestamp(sessionParam.getStartTime());
					session.setEndTimestamp(sessionParam.getEndTime());
					track.addNewSession(session);
					if (talkList.size() > 0)
						session.fillSessionWithTalk(talkList);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trackList;
	}

}
