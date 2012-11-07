package com.reflexit.magiccards.core.sync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererChecklistTest extends TestCase {
	private Properties options;
	private ICoreProgressMonitor monitor;
	private File file;
	private String magicSet;
	private String wall;
	private GatherHelper.StashLoadHandler handler;

	@Override
	public void setUp() throws Exception {
		options = new Properties();
		monitor = ICoreProgressMonitor.NONE;
		file = File.createTempFile("magic", "txt");
		file.deleteOnExit();
		magicSet = "Magic 2013";
		wall = "http://gatherer.wizards.com/pages/search/default.aspx?name=+[%22Bloodfire%22]";
		handler = new GatherHelper.StashLoadHandler();
	}

	public void testDownloadAndCheck() throws FileNotFoundException, MalformedURLException, IOException {
		ParseGathererChecklist.loadSet(wall + "&output=checklist", handler);
		assertEquals(4, handler.getCardCount());
		ArrayList<MagicCard> stash = handler.getPrimary();
		assertEquals(4, stash.size());
		assertEquals("Bloodfire Colossus", stash.get(0).getName());
	}

	public void testMagic13() throws FileNotFoundException, MalformedURLException, IOException {
		ParseGathererChecklist.loadSet(magicSet, handler);
		assertEquals(234, handler.getCardCount());
		ArrayList<MagicCard> stash = handler.getPrimary();
		assertEquals(234, stash.size());
		assertEquals("Acidic Slime", stash.get(0).getName());
	}

	@Override
	protected void tearDown() {
		file.delete();
	}
}
