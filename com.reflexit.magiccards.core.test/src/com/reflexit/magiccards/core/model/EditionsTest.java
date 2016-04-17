package com.reflexit.magiccards.core.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.db.DbActivator;

public class EditionsTest extends TestCase {
	private static final int EDITIONS_SIZE = 155;
	private static final String EDITIONS_FILE = Editions.EDITIONS_FILE;
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
		return Editions.getStoreFile();
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

	@Test
	public void testBlocks() {
		Collection<Edition> list = editions.getEditions();
		assertEquals(EDITIONS_SIZE, list.size());
		for (Edition edition : list) {
			String block = edition.getBlock();
			assertNotNull(block);
			String type = edition.getType();
			assertNotNull(type);
			assertFalse(type.isEmpty());
			String dd = "Duel Decks";
			String ftv = "From the Vault";
			if (edition.getName().startsWith(dd)) {
				assertTrue("Expected " + dd + " but was " + block, block.equals(dd));
				assertTrue("Expected " + ftv + " but was " + type,
						type.equals("Starter"));
			} else if (edition.getName().startsWith(ftv)) {
				assertTrue("Expected " + ftv + " but was " + block + " in " + edition, block.equals(ftv));
				assertTrue("Expected " + ftv + " but was " + type,
						type.equals("Starter"));
			} else
				assertTrue("Expected type but was " + type + " in " + edition, type.equals("Core")
						|| type.equals("Expansion")
						|| type.equals("Reprint")
						|| type.equals("Starter")
						|| type.equals("Online")
						|| type.equals("Modifiers")
						|| type.equals("Un_set")
				//
				);
			assertNotNull("Bad date " + edition, edition.getReleaseDate());
			Date now = new Date();
			assertFalse("Release date in future " + edition, now.before(edition.getReleaseDate()));
		}
	}
}
