package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicLogger;

public class WebUtils {
	private static boolean workOffline = false;

	public static boolean isWorkOffline() {
		return workOffline;
	}

	public static void setWorkOffline(boolean workOffline) {
		WebUtils.workOffline = workOffline;
	}

	public static InputStream openUrl(URL url) throws IOException {
		IOException rt = null;
		for (int i = 0; i < 3; i++) {
			// 3 attempts
			try {
				HttpURLConnection huc = (HttpURLConnection) url.openConnection();
				huc.setConnectTimeout(60 * 1000);
				huc.setReadTimeout(60 * 1000);
				huc.connect();
				InputStream openStream = huc.getInputStream();
				return openStream;
			} catch (IOException e) {
				MagicLogger.log("Connection error on url " + url + ": " + e.getMessage() + ". Attempt " + i);
				rt = e;
				continue;
			}
		}
		if (rt != null) {
			MagicLogger.log("Connection error on url " + url + ": " + rt.getMessage() + ". Giving up");
			throw rt;
		}
		throw new RuntimeException("Not possible");
	}

	public static BufferedReader openUrlReader(URL url) throws IOException {
		InputStream openStream = openUrl(url);
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream, FileUtils.CHARSET_UTF_8));
		return st;
	}
}
