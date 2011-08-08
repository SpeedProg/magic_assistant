package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;

public class ParseGathererLegalityTest extends TestCase {
	private ParseGathererLegality parser;

	@Override
	protected void setUp() {
		parser = new ParseGathererLegality();
	}

	// public void testLoadHtml() {
	// parser.setCardId(153981);
	// parser.setLanguage("Russian");
	// String html = "";
	// html = html.replaceAll("\r?\n", " ");
	// parser.loadHtml(html, new NullProgressMonitor());
	// assertEquals(172550, parser.getLangCardId());
	// }
	public void testLoad() throws IOException {
		parser.setCardId(193867);
		parser.load(new NullProgressMonitor());
		HashMap<String, String> map = parser.getLegalityMap();
		assertEquals(map.get("Legacy"), "Banned");
	}
}
