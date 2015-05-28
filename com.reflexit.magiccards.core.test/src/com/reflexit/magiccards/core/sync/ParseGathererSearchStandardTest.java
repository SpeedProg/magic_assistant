package com.reflexit.magiccards.core.sync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererSearchStandardTest extends TestCase {
	private Properties options;
	private ICoreProgressMonitor monitor;
	private File file;
	private String magicSet;
	private String wall;
	private GatherHelper.StashLoadHandler handler;
	private URL wallUrl;
	private ParseGathererSearchStandard parser;

	@Override
	public void setUp() throws Exception {
		parser = new ParseGathererSearchStandard();
		options = new Properties();
		monitor = ICoreProgressMonitor.NONE;
		file = File.createTempFile("magic", "txt");
		file.deleteOnExit();
		magicSet = "Magic 2013";
		wall = "http://gatherer.wizards.com/pages/search/default.aspx?name=+[%22Bloodfire%22]";
		wallUrl = new URL(wall);
		handler = new GatherHelper.StashLoadHandler();
	}

	public void testDownloadUpdates() throws FileNotFoundException, MalformedURLException, IOException {
		parser.downloadUpdates(magicSet, file.toString(), options, monitor);
		String magicFile = FileUtils.readFileAsString(file);
		assertTrue(magicFile.length() > 0);
		assertTrue(magicFile.contains("Zombie Goliath"));
	}

	public void testDownloadWall() throws FileNotFoundException, MalformedURLException, IOException {
		parser.downloadUpdates(wall, file.toString(), options, monitor);
		String magicFile = FileUtils.readFileAsString(file);
		assertTrue(magicFile.length() > 0);
		assertTrue(magicFile.contains("Bloodfire Colossus"));
	}

	public void testDownloadAndCheck() throws FileNotFoundException, MalformedURLException, IOException {
		parser.loadMultiPageUrl(wallUrl, handler, "unknown", monitor);
		assertEquals(7, handler.getRealCount());
		Collection<MagicCard> stash = handler.getPrimary();
		assertEquals(7, stash.size());
		assertEquals(4, handler.getSecondary().size());
		assertEquals("Bloodfire Colossus", stash.iterator().next().getName());
	}

	public void testMagic13() throws FileNotFoundException, MalformedURLException, IOException {
		parser.loadSingleUrl(GatherHelper.getSearchQuery("standard", magicSet, false), handler);
		// assertEquals(234, handler.getCardCount());
		assertEquals(100, handler.getRealCount());
		Collection<MagicCard> stash = handler.getPrimary();
		assertEquals(100, stash.size());
		MagicCard[] cards = stash.toArray(new MagicCard[stash.size()]);
		assertEquals("Acidic Slime", cards[0].getName());
		MagicCard elf = cards[6];
		assertEquals("Arbor Elf", elf.getName());
		assertEquals(elf.getCost(), "{G}");
		assertEquals(elf.getOracleText(), "{T}: Untap target Forest.");
	}

	public void testDownloadUpdatesWeb() throws FileNotFoundException, MalformedURLException, IOException {
		UpdateCardsFromWeb.downloadUpdates(magicSet, file.toString(), options, monitor);
		String magicFile = FileUtils.readFileAsString(file);
		assertTrue(magicFile.length() > 0);
		assertTrue(magicFile.contains("Zombie Goliath"));
		assertTrue(magicFile.contains("Chris Rahn|147"));
	}

	@Override
	protected void tearDown() {
		file.delete();
	}
}
