package com.reflexit.magiccards.core.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.ParseGathererSets;
import com.reflexit.magiccards.core.sync.ParseSetLegality;
import com.reflexit.magiccards.db.DbActivator;

public class EditionsTest extends TestCase {
	private static final int EDITIONS_SIZE = 124;
	private static final String EDITIONS_FILE = "editions.txt";
	protected Editions editions;

	public void reinit() {
		File file = getExFile();
		if (file.exists()) {
			file.delete();
		}
		InputStream resourceAsStream = DbActivator.class.getClassLoader().getResourceAsStream(EDITIONS_FILE);
		try {
			FileUtils.saveStream(resourceAsStream, getExFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		editions.init();
	}

	public File getExFile() {
		return new File(FileUtils.getStateLocationFile(), EDITIONS_FILE);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		editions = Editions.getInstance();
		reinit();
	}

	@Test
	public void testGetEditions() {
		Collection<Edition> editionsList = editions.getEditions();
		assertEquals(EDITIONS_SIZE, editionsList.size());
	}

	@Test
	public void testGetNameByAbbr() {
		String abbr = editions.getNameByAbbr("EVG");
		assertEquals("Duel Decks: Elves vs. Goblins", abbr);
	}

	@Test
	public void testGetNameByAbbr1E() {
		String name = editions.getNameByAbbr("1E");
		assertEquals("Limited Edition Alpha", name);
	}

	@Test
	public void testGetNameByAbbrLEA() {
		String name = editions.getNameByAbbr("LEA");
		assertEquals("Limited Edition Alpha", name);
	}

	@Test
	public void testGetNameByAbbr8E() {
		String name = editions.getNameByAbbr("8E");
		assertEquals("Eighth Edition", name);
		// Eighth Edition|8ED|8E|July 2003|Core
	}

	@Test
	public void testGetNameByAbbr8ED() {
		String name = editions.getNameByAbbr("8ED");
		assertEquals("Eighth Edition", name);
		// Eighth Edition|8ED|8E|July 2003|Core
	}

	@Test
	public void testAddEdition() {
		Edition ed = editions.addEdition("Ahh", "AHH");
		assertNotNull(ed);
		assertEquals("Ahh", ed.getName());
		assertEquals("AHH", ed.getMainAbbreviation());
	}

	@Test
	public void testContainsName() {
		assertTrue(editions.containsName("Innistrad"));
	}

	@Test
	public void testGetAbbrByName() {
		String abbr = editions.getAbbrByName("Duel Decks: Elves vs. Goblins");
		assertEquals("EVG", abbr);
	}

	@Test
	public void testGetEditionByName() {
		Edition ed = editions.getEditionByName("Conflux");
		assertEquals(ed.getBaseFileName(), "CONFL");
		assertEquals(ed.getMainAbbreviation(), "CON");
	}

	@Test
	public void testGetEditionByNameM14() {
		Edition ed = editions.getEditionByName("Magic 2014");
		assertEquals(ed.getMainAbbreviation(), "M14");
		assertEquals(ed.getName(), "Magic 2014 Core Set");
	}

	@Test
	public void testSave() throws FileNotFoundException {
		getExFile().delete();
		editions.init();
		editions.save();
		assertTrue(getExFile().length() > 0);
	}

	@Test
	public void testGetIdPrefix() {
		String idPrefix = editions.getIdPrefix();
		assertEquals("SET", idPrefix);
	}

	@Test
	public void testLegalities() {
		Edition ed = editions.getEditionByName("Innistrad");
		ed.setFormats("Standard");
		String legalitiesString = ed.getLegalityMap().getFirstLegal().name();
		assertEquals("Standard", legalitiesString);
	}

	@Test
	public void testParseOld() throws IOException {
		getExFile().delete();
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("1x_" + EDITIONS_FILE);
		FileUtils.saveStream(resourceAsStream, getExFile());
		editions.init();
		Collection<Edition> editionsList = editions.getEditions();
		assertEquals(EDITIONS_SIZE, editionsList.size());
	}

	public void testSetLoadingInet() throws IOException {
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

	@Test
	public void testLegalitiesInet() {
		ParseSetLegality.loadAllFormats(ICoreProgressMonitor.NONE);
		Edition ed = editions.getEditionByName("Theros");
		assertEquals(Legality.LEGAL, ed.getLegalityMap().get(Format.STANDARD));
	}
}
