package com.reflexit.magiccards.core.exports;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.test.assist.CardGenerator;

public class ImportUtilsTest extends AbstarctImportTest {
	@Override
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		DataManager.getMagicDBStore().initialize();
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

	public void preimport() {
		try {
			preimport = ImportUtils.performPreImport(new ByteArrayInputStream(line.getBytes()), tableImport, true, deck.getLocation(),
					monitor);
			setout(preimport);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testFindRefByName() {
		MagicCard card = new MagicCard();
		card.setName("Lightning Bolt");
		MagicCard ref = ImportUtils.findRef(card, DataManager.getMagicDBStore());
		assertNotNull(ref);
		assertNotNull(ref.getSet());
	}

	@Test
	public void testFindRefByNameSet() {
		MagicCard card = new MagicCard();
		card.setName("Lightning Bolt");
		card.setSet("Magic 2010");
		MagicCard ref = ImportUtils.findRef(card, DataManager.getMagicDBStore());
		assertNotNull(ref);
		assertEquals("Magic 2010", ref.getSet());
	}

	@Test
	public void testResolveSet() {
		assertEquals("Time Spiral \"Timeshifted\"", ImportUtils.resolveSet("''Timeshifted''").getName());
		assertEquals("Duel Decks: Ajani vs. Nicol Bolas", ImportUtils.resolveSet("Duel decks : Ajani vs. Nicol Bolas").getName());
		assertEquals("Lorwyn", ImportUtils.resolveSet("Token Lorwyn ").getName());
	}

	@Test
	public void testGetFixedName() {
		MagicCard card = new MagicCard();
		card.setName("Aether");
		assertEquals("Æther", ImportUtils.getFixedName(card));
	}

	@Test
	public void testGetSetCandidates() {
		addLine("NAME|SET|COUNT");
		addLine("Counterspell|Bla|2");
		addLine("Light|Foot|1");
		addLine("Light|Duel decks : Ajani vs. Nicol Bolas|1");
		preimport();
		Map<String, String> setCandidates = ImportUtils.getSetCandidates(preimport);
		assertTrue(setCandidates.containsKey("Bla"));
		assertTrue(setCandidates.containsKey("Foot"));
		assertNull(setCandidates.get("Foot"));
		assertEquals(2, setCandidates.size());
		setCandidates.put("Foot", "Lorwyn");
		ImportUtils.fixSets(preimport, setCandidates);
		assertEquals("Bla", card1.getSet());
		assertEquals("Lorwyn", card2.getSet());
	}

	@Test
	public void testUpdateCardReference() {
		MagicCard card = new MagicCard();
		card.setName("Lightning Bolt");
		card.setSet("Magic 2010");
		MagicCardPhysical mcp = new MagicCardPhysical(card, null);
		ImportUtils.updateCardReference(mcp);
		assertNotSame(card, mcp.getCard());
		assertEquals(card.getSet(), mcp.getBase().getSet());
		assertEquals(191089, mcp.getBase().getCardId());
	}

	@Test
	public void testPerformPreview() throws InvocationTargetException, InterruptedException {
		addLine("NAME|SET|COUNT");
		addLine("Counterspell|Bla|2");
		ImportResult performPreview = ImportUtils.performPreview(new ByteArrayInputStream(line.getBytes()), tableImport, true,
				Location.createLocation("test"), monitor);
		List<ICard> values = performPreview.getList();
		assertEquals(1, values.size());
		Object[] fielsValues = performPreview.getFields();
		assertEquals(3, fielsValues.length);
		// assertEquals("Counterspell", fielsValues[0]);
	}

	@Test
	public void testPerformPreImportWithDb() {
		addLine("NAME|SET|COUNT");
		addLine("Counterspell|Foo|2");
		addLine("Light|Foo|1");
		preimport();
		ArrayList<IMagicCard> mdb = new ArrayList<IMagicCard>();
		ImportUtils.performPreImportWithDb(preimport, mdb);
		assertEquals(2, mdb.size());
		Editions.getInstance().addEdition("Foo", null);
		ImportUtils.importIntoDb(mdb);
	}

	@Test
	public void testValidateDbRecords() {
		card1 = new MagicCard();
		card2 = new MagicCard();
		((MagicCard) card2).setName("name");
		card3 = CardGenerator.generateCardWithValues();
		((MagicCard) card3).setCollNumber(0);
		ArrayList<IMagicCard> cards = new ArrayList<IMagicCard>();
		cards.add(card1);
		cards.add(card2);
		cards.add(card3);
		cards.add(CardGenerator.generateCardWithValues());
		ArrayList<String> errors = new ArrayList<String>();
		ImportUtils.validateDbRecords(cards, errors);
		assertEquals(3, errors.size());
		System.err.println(errors);
	}
}
