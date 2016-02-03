package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;

public abstract class AbstractParseHtmlPage {
	public static Charset UTF_8 = Charset.forName("utf-8");
	private String title = "Loading page...";
	private String html;

	public void load() throws IOException {
		load(ICoreProgressMonitor.NONE);
	}

	public synchronized void load(ICoreProgressMonitor monitor) throws IOException {
		monitor.beginTask(getTitle(), 100);
		try {
			URL url = new URL(getUrl());
			String html = WebUtils.openUrlText(url);
			monitor.worked(50);
			if (monitor.isCanceled())
				return;
			setHtml(html);
			loadHtml(html, new SubCoreProgressMonitor(monitor, 50));
		} catch (MagicException e) {
			MagicLogger.log(e);
		} finally {
			monitor.done();
		}
	}

	protected abstract void loadHtml(String html, ICoreProgressMonitor monitor);

	public void loadHtml(ICoreProgressMonitor monitor) {
		if (html == null)
			throw new NullPointerException();
		loadHtml(this.html, monitor);
	}

	protected String extractPatternValue(String html, Pattern pattern, boolean multiple) {
		return ParserHtmlHelper.extractPatternValue(html, pattern, multiple);
	}

	protected abstract String getUrl();

	public AbstractParseHtmlPage() {
		super();
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}
}
