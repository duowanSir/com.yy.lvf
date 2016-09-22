package com.yy.lvf;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import android.media.MediaExtractor;

public class MediaCodecUtils {
	public static MediaExtractor createExtractor(File input) throws IOException {
		MediaExtractor extractor;
		FileInputStream is = null;
		try {
			is = new FileInputStream(input);
			FileDescriptor fd = is.getFD();
			extractor = new MediaExtractor();
			extractor.setDataSource(fd);
		} finally {
			if (is != null) {
				is.close();
				is = null;
			}
		}
		return extractor;
	}
}
