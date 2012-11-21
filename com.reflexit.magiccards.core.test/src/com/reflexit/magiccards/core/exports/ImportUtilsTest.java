package com.reflexit.magiccards.core.exports;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ImportUtilsTest extends AbstarctImportTest {
	@Override
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		DataManager.getCardHandler().getMagicDBFilteredStore().getSize();
	}

	TableImportDelegate tableImport = new TableImportDelegate();
	private ICoreProgressMonitor monitor = ICoreProgressMonitor.NONE;
	private Collection<IMagicCard> preimport;

	private void parse() {
		parse(true, tableImport);
	}

	public void testPerformImport() {
		addLine("NAME|COUNT");
		addLine("Counterspell|2");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
	}

	@Test
	public void testPerformPreImport() throws InvocationTargetException, InterruptedException {
		addLine("NAME|COUNT");
		addLine("Counterspell|2");
		preimport();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
		assertNotNull(card1.getSet());
	}

	public void preimport() throws InvocationTargetException, InterruptedException {
		preimport = ImportUtils.performPreImport(new ByteArrayInputStream(line.getBytes()), tableImport, true, deck.getLocation(), monitor);
		setout(preimport);
	}

	@Test
	public void testFindRefByName() {
		MagicCard card = new MagicCard();
		card.setName("Lightning Bolt");
		MagicCard ref = ImportUtils.findRef(card, DataManager.getCardHandler().getMagicDBStore());
		assertNotNull(ref);
		assertNotNull(ref.getSet());
	}

	@Test
	public void testFindRefByNameSet() {
		MagicCard card = new MagicCard();
		card.setName("Lightning Bolt");
		card.setSet("Magic 2010");
		MagicCard ref = ImportUtils.findRef(card, DataManager.getCardHandler().getMagicDBStore());
		assertNotNull(ref);
		assertEquals("Magic 2010", ref.getSet());
	}

	@Test
	public void testGetFixedSet() {
		fail("Not yet implemented");
	}

	@Test
	public void testResolveSet() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFixedName() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSetCandidates() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateCardReference() {
		fail("Not yet implemented");
	}

	@Test
	public void testPerformPreview() {
		fail("Not yet implemented");
	}

	@Test
	public void testPerformPreImportWithDb() {
		fail("Not yet implemented");
	}

	@Test
	public void testValidateDbRecords() {
		fail("Not yet implemented");
	}

	@Test
	public void testImportIntoDb() {
		fail("Not yet implemented");
	}

	@Test
	public void testFixSets() {
		fail("Not yet implemented");
	}
}
