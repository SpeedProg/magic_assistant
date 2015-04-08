package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public abstract class AbstractParseGathererSearch extends GatherHelper {
	public boolean loadSingleUrl(URL url, GatherHelper.ILoadCardHander handler) throws IOException {
		try {
			String html = WebUtils.openUrlText(url);
			boolean res = processFromReader(FileUtils.openBufferedReader(html), handler);
			return res;
		} catch (IOException e) {
			MagicLogger.log("Loading url exception '" + url + "': " + e.getMessage());
			throw e;
		}
	}

	protected abstract boolean processFromReader(BufferedReader openStringReader, ILoadCardHander handler)
			throws IOException;

	public abstract boolean loadSet(String set, GatherHelper.ILoadCardHander handler, ICoreProgressMonitor mon)
			throws IOException;

	public void loadFile(File file, GatherHelper.ILoadCardHander handler) throws IOException {
		BufferedReader st = FileUtils.openBuferedReader(file);
		processFromReader(st, handler);
		st.close();
	}

	public void loadMultiPageUrl(URL urlOrig, GatherHelper.ILoadCardHander handler, String set,
			ICoreProgressMonitor monitor)
			throws MalformedURLException, IOException {
		monitor.beginTask("Downloading " + set + ":", 10000);
		try {
			int i = 0;
			boolean lastPage = false;
			while (lastPage == false && i < 2000 && monitor.isCanceled() == false) {
				URL url = new URL(urlOrig.toExternalForm() + "&page=" + i);
				lastPage = loadSingleUrl(url, handler);
				i++;
				if (handler.getCardCount() == 0)
					monitor.worked(100);
				else {
					int pages = handler.getCardCount() / 100 + 1;
					monitor.subTask("Page " + i + " of " + pages);
					monitor.worked(10000 / pages);
				}
			}
		} finally {
			monitor.done();
		}
	}
}
