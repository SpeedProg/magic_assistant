package com.reflexit.magiccards.core.seller.test;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.seller.ParseMOTLPrices;
import junit.framework.TestCase;

public class ParseMOTLPricesTest extends TestCase {
	private MemoryCardStore<IMagicCard> store;
	private ParseMOTLPrices parser;
	private ICoreProgressMonitor monitor;

	@Override
	protected void setUp() {
		store = new MemoryCardStore<IMagicCard>();
		parser = new ParseMOTLPrices();
		monitor = ICoreProgressMonitor.NONE;
	}

	public MagicCard addcard(String name, String set) {
		MagicCard card1 = new MagicCard();
		card1.setName(name);
		card1.setSet(set);
		store.add(card1);
		doit();
		return card1;
	}

	public void doit() {
		try {
			parser.updateStore(store, null, 0, monitor);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public void testSwamp() {
		MagicCard card = addcard("Swamp", "Magic 2013");
		assertTrue(0 < card.getDbPrice());
	}

	public void testMagic2010() {
		MagicCard card = addcard("Coat of Arms", "Magic 2010");
		assertTrue(card.getDbPrice() > 3);
	}

	public void testAether() {
		MagicCard card = addcard("Æther Shockwave", "Saviors of Kamigawa");
		assertTrue(0 < card.getDbPrice());
	}

	public void testSixthEdition() {
		MagicCard card = addcard("Armageddon", "Classic Sixth Edition");
		assertTrue(card.getDbPrice() > 0);
	}

	public void testFifthEdition() {
		MagicCard card = addcard("Armageddon", "Fifth Edition");
		assertTrue(card.getDbPrice() > 0);
	}

	public void testTenthEdition() {
		MagicCard card = addcard("Arcane Teachings", "Tenth Edition");
		assertTrue(card.getDbPrice() > 0);
	}
}
