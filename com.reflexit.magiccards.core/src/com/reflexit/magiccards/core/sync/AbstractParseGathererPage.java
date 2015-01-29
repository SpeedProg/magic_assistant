package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;

public abstract class AbstractParseGathererPage {
	public static final String GATHERER_URL_BASE = "http://gatherer.wizards.com/";
	public static Charset UTF_8 = Charset.forName("utf-8");
	private String title = "Loading gatherer info...";
	private String html;

	public void load() throws IOException {
		load(ICoreProgressMonitor.NONE);
	}

	public synchronized void load(ICoreProgressMonitor monitor) throws IOException {
		monitor.beginTask(getTitle(), 100);
		try {
			URL url = new URL(getUrl());
			String html = WebUtils.openUrlAndGetText(url);
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
		Matcher matcher = pattern.matcher(html);
		String value = "";
		while (matcher.find()) {
			String v = matcher.group(1).trim();
			if (value.length() > 0) {
				if (multiple == false)
					throw new IllegalStateException("Multiple pattern found where signle expected");
				value += "\n";
			}
			value += v;
		}
		return value;
	}

	protected abstract String getUrl();

	public AbstractParseGathererPage() {
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
