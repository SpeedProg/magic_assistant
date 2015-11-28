package com.reflexit.magiccards.core.exports;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.exports.ImportUtils.LookupHash;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.unittesting.CardGenerator;

import static org.junit.Assert.*;

public class ImportUtilsTest extends AbstarctImportTest {
	TableImportDelegate tableImport = new TableImportDelegate();
	private ICoreProgressMonitor monitor = ICoreProgressMonitor.NONE;
	private Collection<IMagicCard> preimport;
	private ImportData result;

	private void parse() {
		parse(tableImport);
	}

	@Test
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
			ImportData importData = new ImportData(virtual, deck.getLocation(), line);
			result = ImportUtils.performPreImport(tableImport, importData, monitor);
			preimport = (Collection<IMagicCard>) result.getList();
			if (resolve) {
				ImportUtils.resolve(result.getList());
			}
			setout(preimport);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testFindRefByName() {
		MagicCard card = new MagicCard();
		card.setName("Lightning Bolt");
		MagicCard ref = ImportUtils.findRef(card, getDB());
		assertNotNull(ref);
		assertNotNull(ref.getSet());
	}

	public IDbCardStore<IMagicCard> getDB() {
		return DataManager.getInstance().getMagicDBStore();
	}

	@Test
	public void testFindRefByNameSet() {
		MagicCard card = new MagicCard();
		card.setName("Lightning Bolt");
		card.setSet("Magic 2010");
		MagicCard ref = ImportUtils.findRef(card, getDB());
		assertNotNull(ref);
		assertEquals("Magic 2010", ref.getSet());
	}

	@Test
	public void testResolveSet() {
		assertEquals("Time Spiral \"Timeshifted\"", ImportUtils.resolveSet("''Timeshifted''").getName());
		assertEquals("Duel Decks: Ajani vs. Nicol Bolas",
				ImportUtils.resolveSet("Duel decks : Ajani vs. Nicol Bolas").getName());
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
		addLine("Counterspell2|Bla1|2");
		addLine("Light1|Foot1|1");
		addLine("Light1|Duel decks : Ajani vs. Nicol Bolas|1");
		preimport();
		Map<String, String> setCandidates = ImportUtils.getSetCandidates(preimport);
		assertTrue(setCandidates.containsKey("Bla1"));
		assertTrue(setCandidates.toString(), setCandidates.containsKey("Foot1"));
		assertNull(setCandidates.get("Foot"));
		assertEquals(2, setCandidates.size());
		setCandidates.put("Foot1", "Lorwyn");
		ImportUtils.fixSets(preimport, setCandidates);
		assertEquals("Bla1", card1.getSet());
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
		ImportData improtData = new ImportData(virtual,
				Location.createLocation("test"), line);
		ImportData performPreview = ImportUtils.performPreImport(tableImport, improtData, monitor);
		List values = performPreview.getList();
		if (resolve) {
			ImportUtils.resolve(values);
		}
		assertEquals(1, values.size());
		Object[] fielsValues = performPreview.getFields();
		assertEquals(3, fielsValues.length);
		// assertEquals("Counterspell", fielsValues[0]);
	}

	@Test
	public void testPerformPreImportWithDb() {
		addLine("NAME|SET|COUNT");
		addLine("Counterspell|Foo4|2");
		addLine("Light|Foo4|1");
		resolve = false;
		preimport();
		ArrayList<IMagicCard> mdb = new ArrayList<IMagicCard>();
		ImportUtils.performPreImportWithDb(preimport, mdb, result.getFields());
		assertEquals(2, mdb.size());
		Editions.getInstance().addEdition("Foo4", null);
		ImportUtils.importIntoDb(mdb);
	}

	@Test
	public void testPerformPreImportWithDbOverride() {
		addLine("NAME|SET|ARTIST|COLLNUM|IMAGE_URL");
		addLine("Nighthowler|Magic Game Day Cards|Seb McKinnon|31|http://magiccards.info/scans/en/mgdc/31.jpg");
		resolve = false;
		preimport();
		ArrayList<IMagicCard> mdb = new ArrayList<IMagicCard>();
		Editions.getInstance().addEdition("Magic Game Day Cards", "MGDC");
		ImportUtils.performPreImportWithDb(preimport, mdb, result.getFields());
		// ImportUtils.importIntoDb(mdb);
		assertEquals(1, mdb.size());
		IMagicCard card = mdb.get(0);
		assertEquals("Enchantment Creature - Horror", card.getType());
		assertEquals("Seb McKinnon", card.getArtist());
		assertEquals("Magic Game Day Cards", card.getSet());
		assertEquals(31, card.getCollectorNumberId());
		assertEquals("http://magiccards.info/scans/en/mgdc/31.jpg", card.getBase().getImageUrl());
	}

	@Test
	public void testPerformPreImportWithDbOvNoUrl() {
		addLine("NAME|SET|ARTIST|COLLNUM|TEXT");
		addLine("Nighthowler|Magic Game Day Cards|Seb McKinnon|31|My Text");
		resolve = false;
		preimport();
		ArrayList<IMagicCard> mdb = new ArrayList<IMagicCard>();
		Editions.getInstance().addEdition("Magic Game Day Cards", "MGDC");
		ImportUtils.performPreImportWithDb(preimport, mdb, result.getFields());
		// ImportUtils.importIntoDb(mdb);
		assertEquals(1, mdb.size());
		IMagicCard card = mdb.get(0);
		assertEquals("Enchantment Creature - Horror", card.getType());
		assertEquals("Seb McKinnon", card.getArtist());
		assertEquals("Magic Game Day Cards", card.getSet());
		assertEquals("My Text", card.getText());
		assertEquals("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=405319&type=card",
				card
				.getBase().getImageUrl());
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

	@Test
	public void testLookup() {
		IDbCardStore magicDBStore = DataManager.getCardHandler().getMagicDBStore();
		LookupHash lookupHash = new ImportUtils.LookupHash(magicDBStore);
		assertTrue(lookupHash.getCandidates("Junún Efreet").size() > 0);
	}

	@Test
	public void testPerformPreImportVirtual() throws InvocationTargetException, InterruptedException {
		addLine("NAME|COUNT");
		addLine("Counterspell|2");
		virtual = true;
		preimport();
		assertEquals(1, resSize);
		assertEquals(!virtual, ((MagicCardPhysical) card1).isOwn());
	}

	@Test
	public void testPerformPreImportOwn() throws InvocationTargetException, InterruptedException {
		addLine("NAME|COUNT");
		addLine("Counterspell|2");
		virtual = false;
		preimport();
		assertEquals(1, resSize);
		assertEquals(!virtual, ((MagicCardPhysical) card1).isOwn());
	}

	@Test
	public void testPerformPreImportOwnVar() throws InvocationTargetException, InterruptedException {
		addLine("NAME|COUNT|OWNERSHIP");
		addLine("Counterspell|2|false");
		addLine("Lightning Bolt|2|true");
		virtual = false;
		preimport();
		assertEquals(2, resSize);
		assertEquals(false, ((MagicCardPhysical) card1).isOwn());
		assertEquals(true, ((MagicCardPhysical) card2).isOwn());
	}
}
