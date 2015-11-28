package com.reflexit.magiccards.core.model;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.magiccards.core.test.assist.AbstractMagicTest;
import com.reflexit.unittesting.CardGenerator;

public class MagicCardListTest extends AbstractMagicTest {
	private MagicCard card1;
	private MagicCard card2;
	private MagicCard card3;

	@Before
	public void setUp() throws Exception {
		card1 = CardGenerator.generateCardWithValues();
		card2 = CardGenerator.generateCardWithValues();
		card3 = CardGenerator.generateCardWithValues();
	}

	@Test
	public void testVarArg() {
		MagicCardList list = new MagicCardList(card1, card2);
		assertEquals(2, list.size());
		assertEquals(card1, list.get(0));
	}

	@Test
	public void testArray() {
		MagicCardList list = new MagicCardList(new IMagicCard[] { card1, card2 });
		assertEquals(2, list.size());
		assertEquals(card1, list.get(0));
	}

	@Test
	public void testUniqueSets() {
		String edition1 = "a";
		card1.setSet(edition1);
		card2.setSet(edition1);
		card3.setSet("b");
		MagicCardList list = new MagicCardList(card1, card2, card3);
		Set<?> editions = list.getUnique(MagicCardField.SET);
		assertEquals(2, editions.size());
		assertTrue(editions.contains(edition1));
		assertTrue(editions.contains("b"));
	}

	@Test
	public void testSetAll() {
		String edition1 = "a";
		MagicCardList list = new MagicCardList(card1, card2, card3);
		list.setAll(MagicCardField.SET, edition1);
		Set<?> editions = list.getUnique(MagicCardField.SET);
		assertEquals(1, editions.size());
		assertTrue(editions.contains(edition1));
	}
}
