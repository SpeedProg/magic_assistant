package com.reflexit.magiccards.core.sync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererSearchChecklistTest extends TestCase {
	private ICoreProgressMonitor monitor;
	private File file;
	private String magicSet;
	private String wall;
	private GatherHelper.StashLoadHandler handler;
	private ParseGathererSearchChecklist parser;

	@Override
	public void setUp() throws Exception {
		parser = new ParseGathererSearchChecklist();
		monitor = ICoreProgressMonitor.NONE;
		file = File.createTempFile("magic", "txt");
		file.deleteOnExit();
		magicSet = "Magic 2013";
		wall = "http://gatherer.wizards.com/pages/search/default.aspx?name=+[%22Bloodfire%22]&action=advanced";
		handler = new GatherHelper.StashLoadHandler();
	}

	public void testDownloadAndCheck() throws FileNotFoundException, MalformedURLException, IOException {
		parser.loadSet(wall + "&output=checklist", handler, monitor);
		// assertEquals(4, handler.getRealCount());
		Collection<MagicCard> stash = handler.getPrimary();
		assertEquals(11, stash.size());
		assertEquals("Bloodfire Colossus", stash.iterator().next().getName());
	}

	public void testMagic13() throws FileNotFoundException, MalformedURLException, IOException {
		parser.loadSet(magicSet, handler, monitor);
		// assertEquals(234, handler.getRealCount());
		assertEquals(249, handler.getRealCount());
		Collection<MagicCard> stash = handler.getPrimary();
		assertEquals(249, stash.size());
		assertEquals("Acidic Slime", stash.iterator().next().getName());
	}

	@Override
	protected void tearDown() {
		file.delete();
	}
}
