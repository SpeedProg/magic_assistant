package com.reflexit.magiccards.core.model;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.magiccards.core.model.MagicCardGame.Zone;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.model.storage.PlayingDeck;
import com.reflexit.magiccards.core.model.storage.PlayingDeck.ZonedFilter;
import com.reflexit.unittesting.CardGenerator;

import static org.junit.Assert.*;

public class PlayingDeckTest {
	PlayingDeck deck;
	private MemoryCardStore<IMagicCard> store;
	private int decksize;
	private MagicCardGame mg1;

	@Before
	public void setUp() throws Exception {
		store = new MemoryCardStore<>();
		deck = new PlayingDeck(null);
		for (int i = 0; i < 9; i++) {
			addACard(i);
		}
		addForest();
		deck.setStore(store);
		assertEquals(7, deck.countDrawn());
		decksize = store.getCount();
		mg1 = deck.getList().get(0);
		assertSame(Zone.HAND, mg1.getZone());
	}

	private void addACard(int i) {
		MagicCardPhysical mcp = CardGenerator.generatePhysicalCardWithValues();
		mcp.getBase().setName("Aname " + (i + 1));
		mcp.getBase().setCardId(i + 1000000);
		mcp.setCount(4);
		store.add(mcp);
	}

	private void addForest() {
		MagicCardPhysical mcp = CardGenerator.generatePhysicalCardWithValues();
		MagicCard mc = mcp.getBase();
		mc.setName("Forest");
		mc.setCardId(1000000);
		mc.setCost("");
		mc.setType("Basic Land");
		mcp.setCount(24);
		store.add(mcp);
	}

	@Test
	public void testDraw() {
		deck.draw(1);
		assertEquals(8, deck.countInZone(Zone.HAND));
	}

	@Test
	public void testDraw100() {
		try {
			deck.draw(decksize - 1);
			fail();
		} catch (Exception e) {
		}
	}

	@Test
	public void testScry() {
		deck.scry(2);
		assertEquals(2, deck.countInZone(Zone.SCRY));
	}

	@Test
	public void testScry100() {
		deck.scry(decksize - 3);
		assertEquals(decksize - 7, deck.countInZone(Zone.SCRY));
	}

	@Test
	public void testRestart() {
		deck.restart();
		assertEquals(0, deck.countDrawn());
		assertEquals(decksize, deck.countInZone(Zone.LIBRARY));
	}

	@Test
	public void testShuffle() {
		List<MagicCardGame> before = deck.getListInZone(Zone.LIBRARY);
		List<MagicCardGame> before2 = deck.getListInZone(Zone.LIBRARY);
		assertEquals(before, before2);
		deck.shuffleLibrary();
		List<MagicCardGame> after = deck.getListInZone(Zone.LIBRARY);
		assertNotEquals(before, after);
	}

	@Test
	public void testToZoneList() {
		List<MagicCardGame> list = deck.getListInZone(Zone.HAND);
		deck.toZone(list, Zone.BATTLEFIELD);
		assertEquals(7, deck.countInZone(Zone.BATTLEFIELD));
	}

	@Test
	public void testToZone() {
		MagicCardGame mg = deck.getList().get(0);
		assertSame(Zone.HAND, mg.getZone());
		deck.toZone(mg, Zone.BATTLEFIELD);
		assertSame(Zone.BATTLEFIELD, mg.getZone());
	}

	@Test
	public void testShowZone() {
		MagicCardGame mg = deck.getList().get(0);
		assertSame(Zone.HAND, mg.getZone());
		deck.toZone(mg, Zone.GRAVEYARD);
		assertNotEquals(mg, deck.getElement(0));
		deck.showZone(Zone.GRAVEYARD, true);
		deck.update();
		assertSame(Zone.GRAVEYARD, ((MagicCardGame) deck.getElement(0)).getZone());
	}

	@Test
	public void testCanZone() {
		assertFalse(deck.canZone(Collections.singletonList(mg1), Zone.HAND));
		assertTrue(deck.canZone(Collections.singletonList(mg1), Zone.BATTLEFIELD));
		assertFalse(deck.canZone(Collections.emptyList(), Zone.BATTLEFIELD));
	}

	@Test
	public void testPushback() {
		deck.scry(2);
		List<MagicCardGame> list = deck.getListInZone(Zone.SCRY);
		deck.toZone(list, Zone.LIBRARY);
		deck.pushback(list);
		List<MagicCardGame> lib = deck.getListInZone(Zone.LIBRARY);
		assertEquals(list.get(0), lib.get(lib.size() - 2));
	}

	@Test
	public void testNewturn() {
		deck.toZone(mg1, Zone.BATTLEFIELD);
		deck.tap(Collections.singletonList(mg1));
		assertTrue(mg1.isTapped());
		deck.newTurn();
		assertEquals(7, deck.countInZone(Zone.HAND));
		assertFalse(mg1.isTapped());
	}

	@Test
	public void testGetTurn() {
		assertEquals(1, deck.getTurn());
		deck.newTurn();
		assertEquals(2, deck.getTurn());
	}

	@Test
	public void testTapAndUntap() {
		deck.toZone(mg1, Zone.BATTLEFIELD);
		deck.tap(Collections.singletonList(mg1));
		assertTrue(mg1.isTapped());
		deck.untap();
		assertFalse(mg1.isTapped());
	}

	@Test
	public void testTapInHand() {
		deck.tap(Collections.singletonList(mg1));
		assertFalse(mg1.isTapped());
	}

	@Test
	public void testStoreCount() {
		assertEquals(decksize, ((ICardCountable) deck.getCardStore()).getCount());
	}

	@Test
	public void testCountDrawn() {
		deck.draw(1);
		deck.draw(3);
		MagicCardGame last = deck.getListInZone(Zone.HAND).get(0);
		assertEquals(11, deck.countDrawn());
		assertEquals(11, deck.countInZone(Zone.HAND));
		deck.toZone(last, Zone.LIBRARY);
		assertEquals(10, deck.countDrawn());
		assertEquals(10, deck.countInZone(Zone.HAND));
		last = deck.getListInZone(Zone.HAND).get(0);
		deck.toZone(last, Zone.GRAVEYARD);
		assertEquals(10, deck.countDrawn());
		assertEquals(9, deck.countInZone(Zone.HAND));
	}

	@Test
	public void testMC() {
		store.clear();
		for (int i = 0; i < 20; i++) {
			store.add(CardGenerator.generateCardWithValues());
		}
		deck.setStore(store);
		deck.newGame();
		assertEquals(7, deck.countDrawn());
		decksize = store.getCount();
		mg1 = deck.getList().get(0);
		assertSame(Zone.HAND, mg1.getZone());
		assertEquals(20, decksize);
	}

	@Test
	public void testFilter() {
		deck.draw(1);
		deck.getFilter().setSortField(MagicCardField.NAME, true);
		deck.update();
		assertEquals(8, deck.countInZone(Zone.HAND));
		MagicCardGame mg2 = (MagicCardGame) deck.getElement(0);
		//assertEquals("Aname 1", mg2.getName());
		deck.getFilter().setNoSort();
		deck.update();
		mg2 = (MagicCardGame) deck.getElement(0);
		assertEquals(mg1, mg2);
	}

	@Test
	public void testFilterEquals() {
		ZonedFilter clone = (ZonedFilter) deck.getFilter().clone();
		assertEquals(clone, clone);
		assertEquals(clone, deck.getFilter());
		deck.showZone(Zone.LIBRARY, true);
		deck.update();
		assertNotEquals(clone, deck.getFilter());
	}

	@Test
	public void testNullStore() throws Exception {
		store = new MemoryCardStore<>();
		deck = new PlayingDeck(null);
		deck.newGame();
		assertEquals(0, deck.countDrawn());
		decksize = store.getCount();
		assertEquals(0, decksize);
	}
}
