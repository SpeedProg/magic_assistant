package com.reflexit.magiccards.core.sync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererStandardListTest extends TestCase {
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
		String magicFile = FileUtils.readFileAsString(FileUtils.openFileReader(file));
		assertTrue(magicFile.length() > 0);
		assertTrue(magicFile.contains("Zombie Goliath"));
	}

	public void testDownloadWall() throws FileNotFoundException, MalformedURLException, IOException {
		parser.downloadUpdates(wall, file.toString(), options, monitor);
		String magicFile = FileUtils.readFileAsString(FileUtils.openFileReader(file));
		assertTrue(magicFile.length() > 0);
		assertTrue(magicFile.contains("Bloodfire Colossus"));
	}

	public void testDownloadAndCheck() throws FileNotFoundException, MalformedURLException, IOException {
		parser.loadMultiPageUrl(wallUrl, handler, monitor);
		assertEquals(4, handler.getCardCount());
		ArrayList<MagicCard> stash = handler.getPrimary();
		assertEquals(4, stash.size());
		assertEquals(4, handler.getSecondary().size());
		assertEquals("Bloodfire Colossus", stash.get(0).getName());
	}

	public void testMagic13() throws FileNotFoundException, MalformedURLException, IOException {
		parser.loadSingleUrl(GatherHelper.getSearchQuery("standard", magicSet, false), handler);
		assertEquals(234, handler.getCardCount());
		ArrayList<MagicCard> stash = handler.getPrimary();
		assertEquals(25, stash.size());
		assertEquals("Acidic Slime", stash.get(0).getName());
	}

	@Override
	protected void tearDown() {
		file.delete();
	}
}
