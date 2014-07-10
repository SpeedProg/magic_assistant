package com.reflexit.magiccards.core.sync;

import java.io.IOException;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererSetsTest extends TestCase {
	private ParseGathererSets parser;

	@Override
	protected void setUp() {
		parser = new ParseGathererSets();
	}

	public void testLoadHtml() {
		String html = "	                <b>\r\n"
				+ "	                    Card Set:\r\n"
				+ "	                </b>\r\n"
				+ "	                <p>\r\n"
				+ "	                    <select name=\"ctl00$ctl00$MainContent$Content$SearchControls$setAddText\" id=\"ctl00_ctl00_MainContent_Content_SearchControls_setAddText\">\r\n"
				+ "	<option value=\"\"></option>\r\n" + "	<option value=\"Alara Reborn\">Alara Reborn</option>\r\n"
				+ "	<option value=\"Alliances\">Alliances</option>\r\n" + "	<option value=\"Antiquities\">Antiquities</option>\r\n"
				+ "	...\r\n" + "	</select>";
		parser.loadHtml(html, ICoreProgressMonitor.NONE);
		assertTrue(hasEdition("Alara Reborn"));
	}

	public boolean hasEdition(String set) {
		return Editions.getInstance().getEditionByName(set) != null;
	}

	public void testLoad() throws IOException {
		parser.load(ICoreProgressMonitor.NONE);
		assertTrue(parser.getAll().contains("Magic 2015 Core Set"));
	}
}
