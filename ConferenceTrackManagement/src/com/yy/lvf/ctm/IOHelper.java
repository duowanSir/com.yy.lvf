package com.yy.lvf.ctm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IOHelper {
	public static String readSessionPacksackProperties() {
		File propertiesFile = new File("res" + File.separator + "session.packsack.properties");
		if (!propertiesFile.exists()) {
			return null;
		}
		RandomAccessFile raf = null;
		StringBuilder sb = new StringBuilder();
		try {
			raf = new RandomAccessFile(propertiesFile, "r");
			String line;
			while ((line = raf.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static List<String> readInput(String filename) {
		if (filename == null || "".equals(filename)) {
			filename = CtmParamHelper.getInstance().getInputFilePath();
		}
		List<String> inputList = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				inputList.add(strLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inputList;
	}

	/**
	 * 根据读取行生成合法的Talk对象
	 */
	public static List<Talk> generateValidTalks(List<String> talkList) {
		if (talkList == null || talkList.isEmpty()) {
			return null;
		}

		List<Talk> validTalksList = new ArrayList<>();
		int maxTalkTime = CtmParamHelper.getInstance().getMaxTalkMinutes();
		int minTalkTime = CtmParamHelper.getInstance().getMinTalkMinutes();
		int talktime = 0;
		Pattern pattern = Pattern.compile("(.*)(\\s){1}([0-2]?[0-9]?[0-9]{1}"+CtmParamHelper.getInstance().getMinuteSuffix()+"|"+CtmParamHelper.getInstance().getLightningSuffix()+")\\b");
		for (String talk : talkList) {
			talk = talk.replaceAll("\\s+", " ").trim();
			Matcher matcher = pattern.matcher(talk);
			if (!matcher.matches()) {
				continue;
			}
			talktime = generateTalkTime(matcher.group(3));
			if (talktime <= maxTalkTime && talktime >= minTalkTime) {
				validTalksList.add(new Talk(talk, talktime));
			} else {
				System.out.println("IOHelper.generateValidTalks():" + talk + " 超出时长范围");
			}
		}
		//		for (Talk talk : validTalksList) {
		//			System.out.println(talk.toString());
		//		}
		return validTalksList;
	}

	private static int generateTalkTime(String endingStr) {
		String minuteSuffix = CtmParamHelper.getInstance().getMinuteSuffix();
		String lightningSuffix = CtmParamHelper.getInstance().getLightningSuffix();
		int talktime = 0;
		try {
			if (endingStr.endsWith(minuteSuffix)) {
				talktime = Integer.parseInt(endingStr.substring(0, endingStr.indexOf(minuteSuffix)));
			} else if (endingStr.endsWith(lightningSuffix)) {
				talktime = CtmParamHelper.getInstance().getLightningMinutes();
			}
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		return talktime;
	}

}
