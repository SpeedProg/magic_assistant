package com.reflexit.magiccards.core.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.sync.ParseGathererSets;
import com.reflexit.magiccards.core.sync.ParseSetLegality;
import com.reflexit.magiccards.db.DbActivator;

public class EditionsTest extends TestCase {
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
		IPath path = Activator.getStateLocationAlways().append(EDITIONS_FILE);
		File file = path.toFile();
		return file;
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
		assertEquals(105, editionsList.size());
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
	public void testSave() throws FileNotFoundException {
		getExFile().delete();
		editions.init();
		editions.save();
		assertEquals(0, getExFile().length());
	}

	@Test
	public void testGetIdPrefix() {
		String idPrefix = editions.getIdPrefix();
		assertEquals("SET", idPrefix);
	}

	@Test
	public void testLegalities() {
		Edition ed = editions.getEditionByName("Innistrad");
		ed.clearLegality();
		ed.addFormat("Standard");
		ed.addFormat("Extended");
		String legalitiesString = ed.getFormatString();
		assertEquals("Standard, Extended", legalitiesString);
	}

	@Test
	public void testParseOld() throws IOException {
		getExFile().delete();
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("1x_" + EDITIONS_FILE);
		FileUtils.saveStream(resourceAsStream, getExFile());
		editions.init();
		Collection<Edition> editionsList = editions.getEditions();
		assertEquals(105, editionsList.size());
	}

	public void testSetLoadingInet() throws IOException {
		Collection<Edition> oldList = editions.getEditions();
		getExFile().delete();
		editions.init();
		editions.save();
		ParseGathererSets parser = new ParseGathererSets();
		parser.load(new NullProgressMonitor());
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
		ParseSetLegality.loadAllFormats(new NullProgressMonitor());
		Edition ed = editions.getEditionByName("Innistrad");
		String legalitiesString = ed.getFormatString();
		assertEquals("Standard, Modern, Extended", legalitiesString);
	}
}
