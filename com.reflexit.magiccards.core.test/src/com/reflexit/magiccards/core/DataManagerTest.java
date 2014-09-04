package com.reflexit.magiccards.core;

import static org.junit.Assert.assertNotEquals;

import java.util.Collections;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.model.utils.CardGenerator;

public class DataManagerTest extends TestCase {
	private final DataManager dm = new DataManager();
	static final int CARD_ID_MYSTICDECREE = 2952;
	private MagicCardPhysical card;
	private Location loc;
	private MemoryCardStore<IMagicCard> store;

	@Override
	protected void setUp() throws Exception {
		dm.waitForInit(10);
		card = generatePhyCard(CARD_ID_MYSTICDECREE);
		card.setOwn(true);
		loc = new Location("bla");
		store = new MemoryCardStore<IMagicCard>();
		store.setLocation(loc);
	}

	public MagicCard generateCard() {
		return CardGenerator.generateCardWithValues();
	}

	public MagicCardPhysical generatePhyCard() {
		return CardGenerator.generatePhysicalCardWithValues();
	}

	public MagicCardPhysical generatePhyCard(int cardId) {
		IMagicCard card = DataManager.getMagicDBStore().getCard(cardId);
		return CardGenerator.generatePhysicalCardWithValues(card);
	}

	@Test
	public void testSplitCard() {
		card.setCount(2);
		MagicCardPhysical left = dm.split(card, 1);
		assertEquals(1, left.getCount());
		assertEquals(1, card.getCount());
		card.setCount(2);
		assertEquals(1, left.getCount());
	}

	@Test
	public void testSplitCard4() {
		card.setCount(4);
		MagicCardPhysical left = dm.split(card, 2);
		assertEquals(2, left.getCount());
		assertEquals(2, card.getCount());
		card.setCount(3);
		assertEquals(2, left.getCount());
		assertEquals(3, card.getCount());
	}

	@Test
	public void testSplitCardMore() {
		card.setCount(2);
		MagicCardPhysical left = dm.split(card, 2);
		assertNull(left);
	}

	@Test
	public void testCopyCards() {
		card.setOwn(false);
		store.setVirtual(true);
		dm.copyCards(Collections.singletonList(card), store, loc);
		assertEquals(1, store.size());
		MagicCardPhysical card1 = (MagicCardPhysical) store.get(0);
		assertNotEquals(card, card1);
		assertTrue(card + " vs " + card1, card.matching(card1));
	}

	@Test
	public void testCopyCardsOwn() {
		card.setOwn(true);
		store.setVirtual(true);
		dm.copyCards(Collections.singletonList(card), store, loc);
		assertEquals(1, store.size());
		MagicCardPhysical card1 = (MagicCardPhysical) store.get(0);
		assertNotEquals(card, card1);
		assertTrue(card + " vs " + card1, card.getBase().equals(card1.getBase()));
		assertFalse(card.isOwn() == card1.isOwn());
	}

	@Test
	public void testCopyCardsVi() {
		store.setVirtual(false);
		try {
			dm.copyCards(Collections.singletonList(card), store, loc);
			fail("Should throw ex");
		} catch (MagicException e) {
			// good
		}
	}

	//
	// @Test
	// public void testInstantiateCollectionOfIMagicCard() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testInstantiateIMagicCardICardStore() {
	// fail("Not yet implemented");
	// }
	//
	@Test
	public void testMoveCardsVirVir() {
		card.setOwn(false);
		store.setVirtual(true);
		dm.moveCards(Collections.singletonList(card), store, loc);
		assertEquals(1, store.size());
		MagicCardPhysical card1 = (MagicCardPhysical) store.get(0);
		assertTrue(card + " vs " + card1, card.matching(card1));
	}
	// @Test
	// public void testSplitCardsMapOfIMagicCardInteger() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testAddICardStoreCollection() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testRemoveICardStoreCollection() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testRemoveMagicCardPhysical() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testAddMagicCardPhysical() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testMove() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testUpdateMagicCardPhysical() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testUpdateMagicCard() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testUpdateICardStoreMagicCardPhysical() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testUpdateIMagicCard() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testUpdateList() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testReconcile() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testReconcileIterable() {
	// fail("Not yet implemented");
	// }
}
