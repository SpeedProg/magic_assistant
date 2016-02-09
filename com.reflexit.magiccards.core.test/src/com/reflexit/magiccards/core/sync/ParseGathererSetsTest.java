package com.reflexit.magiccards.core.sync;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.Edition;
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

	public File getExFile() {
		return Editions.getStoreFile();
	}

	public void testSetLoadingInet() throws IOException {
		Editions editions = Editions.getInstance();
		Collection<Edition> oldList = editions.getEditions();
		getExFile().delete();
		editions.init();
		editions.save();
		ParseGathererSets parser = new ParseGathererSets();
		parser.load(ICoreProgressMonitor.NONE);
		Collection<Edition> editionsList = editions.getEditions();
		assertTrue("Expected more than 103 but was " + editionsList.size(), editionsList.size() > 103);
		for (Iterator iterator = editionsList.iterator(); iterator.hasNext();) {
			Edition edition = (Edition) iterator.next();
			if (!oldList.contains(edition)) {
				System.err.println("NEW: " + edition.getName() + " " + edition.getMainAbbreviation());
			}
		}
		for (Iterator iterator = oldList.iterator(); iterator.hasNext();) {
			Edition edition = (Edition) iterator.next();
			if (!editionsList.contains(edition)) {
				System.err.println("OLD: " + edition.getName() + " " + edition.getMainAbbreviation());
			}
		}
	}
}
