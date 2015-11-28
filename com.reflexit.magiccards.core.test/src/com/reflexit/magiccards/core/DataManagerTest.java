package com.reflexit.magiccards.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.test.assist.Profiler;
import com.reflexit.unittesting.TestFileUtils;

import static org.junit.Assert.assertNotEquals;

@FixMethodOrder(MethodSorters.JVM)
public class DataManagerTest extends TestCase {
	private DataManager dm;
	static final int CARD_ID_MYSTICDECREE = 2952;
	private MagicCardPhysical card;
	private CardCollection deck2;
	private CardCollection deck1;
	private ICardStore<IMagicCard> store2;
	static boolean reset = true;
	static int i = 0;

	static void init() {
		if (reset) {
			TestFileUtils.resetDb();
			reset = false;
		}
	}

	@Override
	protected void setUp() throws Exception {
		init();
		dm = DataManager.getInstance();
		dm.waitForInit(10);
		dm.getLibraryCardStore();
		deck1 = createDeck(true);
		deck2 = createDeck(true);
		store2 = deck2.getStore();
		card = phyCard(CARD_ID_MYSTICDECREE, deck1.getLocation());
		dm.add(card);
	}

	@Override
	protected void tearDown() throws Exception {
		deck1.remove();
		deck2.remove();
		super.tearDown();
	}

	public CardCollection createDeck(boolean vir) {
		i++;
		CardCollection deck2 = dm.getModelRoot().getDeckContainer()
				.addDeck("bla" + i, vir);
		ICardStore<IMagicCard> store2 = deck2.getStore();
		assertNotNull(store2);
		return deck2;
	}

	public MagicCardPhysical phyCard(int cardId, Location loc) {
		IMagicCard base = dm.getMagicDBStore().getCard(cardId);
		assertNotNull("Cannot find " + cardId + " " + FileUtils.getMagicCardsDir() + " "
				+ dm.getMagicDBStore().size(), base);
		MagicCardPhysical card = new MagicCardPhysical(base, loc);
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
		dm.copyCards(Collections.singletonList(card), store2);
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
		//deck2.setVirtual(vir);
		deck2.remove();
		deck2 = createDeck(vir);
		store2 = deck2.getStore();
	}

	@Test
	public void testCopyCardsOwn() {
		card.setOwn(true);
		setVirtual(true);
		dm.copyCards(Collections.singletonList(card), store2);
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
			dm.copyCards(Collections.singletonList(card), store2);
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
		dm.moveCards(Collections.singletonList(card), store2);
		assertEquals(1, deck1.getStore().size());
		assertEquals(1, store2.size());
		MagicCardPhysical card1 = getFirst();
		assertTrue(card + " vs " + card1, card.matching(card1));
	}

	@Test
	public void testMoveCards() {
		card.setOwn(true);
		setVirtual(false);
		dm.moveCards(Collections.singletonList(card), store2);
		assertEquals(0, deck1.getStore().size());
		assertEquals(1, store2.size());
		MagicCardPhysical card1 = getFirst();
		assertTrue(card + " vs " + card1, card.matching(card1));
	}

	@Test
	public void testMoveCards2() {
		card.setOwn(true);
		setVirtual(false);
		dm.moveCards(Collections.singletonList(card), store2);
		assertEquals(0, deck1.getStore().size());
		assertEquals(1, store2.size());
		MagicCardPhysical card1 = getFirst();
		assertTrue(card + " vs " + card1, card.matching(card1));
	}

	@Test
	public void testMoveCardsSep() {
		card.setOwn(true);
		setVirtual(false);
		CardCollection deck3 = createDeck(true);
		MagicCardPhysical cardA = phyCard(CARD_ID_MYSTICDECREE, deck3.getLocation());
		cardA.setCount(2);
		dm.add(cardA);
		assertEquals(1, deck1.getStore().size());
		assertEquals(1, deck3.getStore().size());
		dm.moveCards(list(card, cardA), store2);
		assertEquals(0, deck1.getStore().size());
		assertEquals(0, deck3.getStore().size());
		assertEquals(1, store2.size());
		MagicCardPhysical card1 = getFirst();
		assertTrue(card + " vs " + card1, card.matching(card1));
		assertEquals(3, card1.getCount());
	}

	@Test
	public void testMoveCardsDB() {
		card.setOwn(false);
		dm.moveCards(list(card.getBase()), store2);
		assertEquals(1, deck1.getStore().size());
		assertEquals(1, store2.size());
		MagicCardPhysical card1 = getFirst();
		assertTrue(card + " vs " + card1, card.matching(card1));
		assertEquals(1, card1.getCount());
	}

	private Collection list(Object... args) {
		ArrayList list = new ArrayList<Object>();
		for (Object object : args) {
			list.add(object);
		}
		return list;
	}

	@Test
	public void testMoveCardsOwnVir() {
		card.setOwn(true);
		setVirtual(true);
		try {
			dm.moveCards(Collections.singletonList(card), store2);
			fail();
		} catch (MagicException e) {
			// good
		}
	}

	@Test
	public void testAddMagicCardPhysical() {
		MagicCardPhysical cardA = phyCard(CARD_ID_MYSTICDECREE, deck2.getLocation());
		cardA.setCount(2);
		MagicCard base = cardA.getBase();
		int x = base.getOwnCount();
		dm.add(cardA);
		MagicCardPhysical card1 = getFirst();
		assertTrue(cardA + " vs " + card1, card.matching(card1));
		assertEquals(x + 2, base.getOwnCount());
	}

	@Test
	public void testAddICardStoreCollection() {
		MagicCardPhysical cardA = phyCard(CARD_ID_MYSTICDECREE, deck2.getLocation());
		dm.add(Collections.singleton(cardA), store2);
		MagicCardPhysical card1 = getFirst();
		assertTrue(cardA + " vs " + card1, card.matching(card1));
	}

	@Test
	public void testRemoveICardStoreCollection() {
		assertEquals(1, deck1.getStore().size());
		dm.remove(Collections.singleton(card), deck1.getStore());
		assertEquals(0, deck1.getStore().size());
	}

	@Test
	public void testRemoveMagicCardPhysical() {
		MagicCard base = card.getBase();
		int x = base.getOwnCount();
		assertEquals(1, deck1.getStore().size());
		dm.remove(card);
		assertEquals(0, deck1.getStore().size());
		assertEquals(x - 1, base.getOwnCount());
	}

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
	@Test
	public void testMaterialize() {
		MagicCardPhysical card2 = new MagicCardPhysical(card, deck2.getLocation());
		card2.setOwn(false);
		Collection<? extends IMagicCard> materialize = dm.materialize(Collections.singletonList(card2),
				deck1.getStore());
		assertEquals(1, materialize.size());
		MagicCardPhysical card1 = (MagicCardPhysical) materialize.iterator().next();
		assertTrue(card + " vs " + card1, card.matching(card1));
		assertEquals(true, card1.isOwn());
	}

	public void testMaterializeCount() {
		dm.remove(card);
		card.setCount(3);
		dm.add(card);
		MagicCardPhysical card2 = new MagicCardPhysical(card, deck2.getLocation());
		card2.setOwn(false);
		card2.setCount(1);
		Collection<? extends IMagicCard> materialize = dm.materialize(Collections.singletonList(card2),
				deck1.getStore());
		assertEquals(1, materialize.size());
		MagicCardPhysical card1 = (MagicCardPhysical) materialize.iterator().next();
		assertTrue(card + " vs " + card1, card.matching(card1));
		assertEquals(1, card1.getCount());
	}

	public void testMaterializeCountMis() {
		card.setCount(1);
		MagicCardPhysical card2 = new MagicCardPhysical(card, deck2.getLocation());
		card2.setOwn(false);
		card2.setCount(3);
		Collection<? extends IMagicCard> materialize = dm.materialize(Collections.singletonList(card2),
				deck1.getStore());
		assertEquals(2, materialize.size());
		MagicCardPhysical card1 = (MagicCardPhysical) materialize.iterator().next();
		assertTrue(card + " vs " + card1, card.matching(card1));
		assertEquals(1, card1.getCount());
	}

	@Test
	public void testMaterializeNotFound() {
		card.setOwn(false);
		Collection<? extends IMagicCard> materialize = dm.materialize(Collections.singletonList(card),
				deck2.getStore());
		assertEquals(1, materialize.size());
		MagicCardPhysical card1 = (MagicCardPhysical) materialize.iterator().next();
		assertTrue(card + " vs " + card1, card.matching(card1));
		assertEquals(false, card1.isOwn());
	}

	@Test
	public void testMaterializeOwnNotFound() {
		card.setOwn(false);
		Collection<? extends IMagicCard> materialize = dm.materialize(Collections.singletonList(card),
				deck2.getStore());
		assertEquals(1, materialize.size());
		MagicCardPhysical card1 = (MagicCardPhysical) materialize.iterator().next();
		assertTrue(card + " vs " + card1, card.matching(card1));
		assertEquals(false, card1.isOwn());
	}

	public void testMaterializeOtherNotOwn() {
		dm.remove(card);
		card.setCount(3);
		card.setOwn(false);
		dm.add(card);
		MagicCardPhysical card2 = new MagicCardPhysical(card, deck2.getLocation());
		card2.setOwn(false);
		card2.setCount(1);
		Collection<? extends IMagicCard> materialize = dm.materialize(Collections.singletonList(card2),
				deck1.getStore());
		assertEquals(1, materialize.size());
		MagicCardPhysical card1 = (MagicCardPhysical) materialize.iterator().next();
		assertTrue(card + " vs " + card1, card.matching(card1));
		assertEquals(1, card1.getCount());
		assertEquals(false, card1.isOwn());
	}

	public void testMaterializeOtherNotOwnAndAnother() {
		dm.remove(card);
		card.setCount(3);
		card.setOwn(false);
		dm.add(card);
		card.setCount(2);
		card.setOwn(true);
		dm.add(card);
		MagicCardPhysical card2 = new MagicCardPhysical(card, deck2.getLocation());
		card2.setOwn(false);
		card2.setCount(1);
		Collection<? extends IMagicCard> materialize = dm.materialize(Collections.singletonList(card2),
				deck1.getStore());
		assertEquals(1, materialize.size());
		MagicCardPhysical card1 = (MagicCardPhysical) materialize.iterator().next();
		assertTrue(card + " vs " + card1, card.matching(card1));
		assertEquals(1, card1.getCount());
		assertEquals(true, card1.isOwn());
	}

	public void testMaterializeOtherNotOwnAndAnother2() {
		dm.remove(card);
		card.setCount(2);
		card.setOwn(true);
		dm.add(card);
		card.setCount(3);
		card.setOwn(false);
		dm.add(card);
		MagicCardPhysical card2 = new MagicCardPhysical(card, deck2.getLocation());
		card2.setOwn(false);
		card2.setCount(1);
		Collection<? extends IMagicCard> materialize = dm.materialize(Collections.singletonList(card2),
				deck1.getStore());
		assertEquals(1, materialize.size());
		MagicCardPhysical card1 = (MagicCardPhysical) materialize.iterator().next();
		assertEquals(1, card1.getCount());
		assertEquals(true, card1.isOwn());
	}

	public static void main(String[] args) {
		Profiler.testTimeAndMem(() -> init(), 1300, 18 * 1024 * 1024);
		// Profiler.sleep(10 * 1000);
		System.err.println("done");
	}
}
