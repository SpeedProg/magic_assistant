package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererSetsTest extends TestCase {
	private ParseGathererSets parser;

	@Override
	protected void setUp() {
		parser = new ParseGathererSets();
	}

	public void testLoadHtml() {
		String html = "	                <b>\r\n"
				+ "	                    Filter Card Set:\r\n"
				+ "	                </b>\r\n"
				+ "	                <p>\r\n"
				+ "	                    <select name=\"ctl00$ctl00$MainContent$Content$SearchControls$setAddText\" id=\"ctl00_ctl00_MainContent_Content_SearchControls_setAddText\">\r\n"
				+ "	<option value=\"\"></option>\r\n" + "	<option value=\"Alara Reborn\">Alara Reborn</option>\r\n"
				+ "	<option value=\"Alliances\">Alliances</option>\r\n" + "	<option value=\"Antiquities\">Antiquities</option>\r\n"
				+ "	...\r\n" + "	</select>";
		parser.loadHtml(ICoreProgressMonitor.NONE);
		assertTrue(hasEdition("Alara Reborn"));
	}

	public boolean hasEdition(String set) {
		Collection<Edition> editions = Editions.getInstance().getEditions();
		for (Iterator iterator = editions.iterator(); iterator.hasNext();) {
			Edition edition = (Edition) iterator.next();
			if (edition.getName().equals(set))
				return true;
			// System.err.println(edition.getName() + " " +
			// edition.getMainAbbreviation());
		}
		return false;
	}

	public void testLoad() throws IOException {
		parser.load(ICoreProgressMonitor.NONE);
		assertTrue(hasEdition("Alara Reborn"));
	}
}
