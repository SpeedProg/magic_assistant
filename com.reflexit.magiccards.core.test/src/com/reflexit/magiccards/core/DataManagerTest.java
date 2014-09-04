package com.reflexit.magiccards.core;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.utils.CardGenerator;

public class DataManagerTest extends TestCase {
	static final int CARD_ID_MYSTICDECREE = 2952;
	private MagicCardPhysical card;

	@Override
	protected void setUp() throws Exception {
		DataManager.waitForInit(10);
		card = generatePhyCard(CARD_ID_MYSTICDECREE);
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
		MagicCardPhysical left = DataManager.split(card, 1);
		assertEquals(1, left.getCount());
		assertEquals(1, card.getCount());
		card.setCount(2);
		assertEquals(1, left.getCount());
	}

	@Test
	public void testSplitCard4() {
		card.setCount(4);
		MagicCardPhysical left = DataManager.split(card, 2);
		assertEquals(2, left.getCount());
		assertEquals(2, card.getCount());
		card.setCount(3);
		assertEquals(2, left.getCount());
		assertEquals(3, card.getCount());
	}

	@Test
	public void testSplitCardMore() {
		card.setCount(2);
		MagicCardPhysical left = DataManager.split(card, 2);
		assertNull(left);
	}
	//
	// @Test
	// public void testCopyCards() {
	// fail("Not yet implemented");
	// }
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
	// @Test
	// public void testMoveCards() {
	// fail("Not yet implemented");
	// }
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
