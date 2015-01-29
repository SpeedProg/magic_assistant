package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicException;
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
		int maxAttempts = 3;
		for (int i = 0; i < maxAttempts; i++) {
			// 3 attempts
			try {
				URLConnection openConnection = url.openConnection();
				if (openConnection instanceof HttpURLConnection) {
					if (WebUtils.isWorkOffline())
						throw new MagicException("Online updates are disabled");
					HttpURLConnection huc = (HttpURLConnection) openConnection;
					huc.setRequestProperty("Accept-Charset", FileUtils.UTF8);
					huc.setRequestProperty("Accept-Language", "en_US");
					// huc.setRequestProperty("User-Agent",
					// "Mozilla/5.0 (Windows NT 5.1; rv:19.0; en_US) Gecko/20100101 Firefox/19.0");
					huc.setConnectTimeout(60 * 1000);
					huc.setReadTimeout(60 * 1000);
					huc.connect();
				} else {
					// not http connection
					i = maxAttempts;
				}
				InputStream openStream = openConnection.getInputStream();
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
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream, FileUtils.CHARSET_UTF_8),
				FileUtils.DEFAULT_BUFFER_SIZE);
		return st;
	}

	public static String openUrlText(URL url) throws IOException {
		BufferedReader st = openUrlReader(url);
		try {
			return FileUtils.readFileAsString(st);
		} finally {
			try {
				st.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
