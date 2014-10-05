package com.reflexit.magiccards.core;

import static org.junit.Assert.assertNotEquals;

import java.util.Collections;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.test.assist.TestFileUtils;

public class DataManagerTest extends TestCase {
	private final static DataManager dm = new DataManager();
	static final int CARD_ID_MYSTICDECREE = 2952;
	private MagicCardPhysical card;
	private CardCollection deck2;
	private CardCollection deck1;
	private ICardStore<IMagicCard> store2;
	static int i = 0;

	static void init() {
		TestFileUtils.resetDb();
		dm.waitForInit(10);
		i = 1;
	}

	@Override
	protected void setUp() throws Exception {
		if (i == 0) {
			init();
		}
		dm.getLibraryCardStore();
		deck1 = createDeck();
		deck2 = createDeck();
		store2 = deck2.getStore();
		card = phyCard(CARD_ID_MYSTICDECREE, deck1.getLocation());
	}

	public CardCollection createDeck() {
		i++;
		CardCollection deck2 = dm.getModelRoot().getDeckContainer()
				.addDeck("bla" + i);
		deck2.open();
		ICardStore<IMagicCard> store2 = deck2.getStore();
		assertNotNull(store2);
		return deck2;
	}

	public MagicCardPhysical phyCard(int cardId, Location loc) {
		IMagicCard base = DataManager.getMagicDBStore().getCard(cardId);
		card = new MagicCardPhysical(base, loc);
		card.setOwn(true);
		card.setCount(1);
		return card;
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
		setVirtual(true);
		dm.copyCards(Collections.singletonList(card), store2,
				store2.getLocation());
		assertEquals(1, store2.size());
		MagicCardPhysical card1 = getFirst();
		assertNotEquals(card, card1);
		assertTrue(card + " vs " + card1, card.matching(card1));
	}

	protected MagicCardPhysical getFirst() {
		MagicCardPhysical card1 = (MagicCardPhysical) store2.iterator().next();
		return card1;
	}

	protected void setVirtual(boolean vir) {
		deck2.setVirtual(vir);
	}

	@Test
	public void testCopyCardsOwn() {
		card.setOwn(true);
		setVirtual(true);
		dm.copyCards(Collections.singletonList(card), store2,
				store2.getLocation());
		assertEquals(1, store2.size());
		MagicCardPhysical card1 = getFirst();
		assertNotEquals(card, card1);
		assertTrue(card + " vs " + card1, card.getBase()
				.equals(card1.getBase()));
		assertFalse(card.isOwn() == card1.isOwn());
	}

	@Test
	public void testCopyCardsVi() {
		setVirtual(false);
		try {
			dm.copyCards(Collections.singletonList(card), store2,
					store2.getLocation());
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
		setVirtual(true);
		dm.moveCards(Collections.singletonList(card), store2,
				store2.getLocation());
		assertEquals(0, deck1.getStore().size());
		assertEquals(1, store2.size());
		MagicCardPhysical card1 = getFirst();
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
