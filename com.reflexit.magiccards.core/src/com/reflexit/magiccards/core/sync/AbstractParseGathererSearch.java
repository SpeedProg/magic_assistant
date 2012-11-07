package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public abstract class AbstractParseGathererSearch extends GatherHelper {
	public boolean loadSingleUrl(URL url, GatherHelper.ILoadCardHander handler) throws IOException {
		try {
			BufferedReader st = UpdateCardsFromWeb.openUrlReader(url);
			String html = FileUtils.readFileAsString(st);
			st.close();
			boolean res = processFromReader(FileUtils.openStringReader(html), handler);
			return res;
		} catch (IOException e) {
			MagicLogger.log("Loading url exception: " + url + ": " + e.getMessage());
			throw e;
		}
	}

	protected abstract boolean processFromReader(BufferedReader openStringReader, ILoadCardHander handler) throws IOException;

	public abstract boolean loadSet(String set, GatherHelper.ILoadCardHander handler, ICoreProgressMonitor mon) throws IOException;

	public void loadFile(File file, GatherHelper.ILoadCardHander handler) throws IOException {
		BufferedReader st = FileUtils.openFileReader(file);
		processFromReader(st, handler);
		st.close();
	}
}
