package com.reflexit.magiccards.core.model;

import java.util.Collection;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.test.assist.CardGenerator;

public class CardGroupTest extends TestCase {
	private CardGroup group;
	private IMagicCard[] cards;
	private CardGroup set;

	@Override
	public void setUp() {
		group = new CardGroup(MagicCardField.RARITY, Rarity.COMMON);
		set = new CardGroup(MagicCardField.SET, "Lorwyn");
		cards = new IMagicCard[3];
		for (int j = 0; j < cards.length; j++) {
			cards[j] = generateCard();
			group.add(cards[j]);
		}
	}

	public void populateGroup(CardGroup g, Object[] some) {
		for (int i = 0; i < some.length; i++) {
			Object object = some[i];
			g.add(object);
		}
	}

	public IMagicCard[] populateGroup(CardGroup g, int len) {
		IMagicCard[] res = new IMagicCard[len];
		for (int i = 0; i < len; i++) {
			IMagicCard object = generateCard();
			g.add(object);
			res[i] = object;
		}
		return res;
	}

	public IMagicCard generateCard() {
		return CardGenerator.generateCardWithValues();
	}

	public void subtestGetBase(ICardField field, Object expected) {
		subtestGetBase(field, String.valueOf(expected), expected);
	}

	public void subtestGetBase(ICardField field, String value, Object expected) {
		for (int j = 0; j < cards.length; j++) {
			((ICardModifiable) cards[j]).setObjectByField(field, value);
		}
		group.refresh();
		assertEquals(expected, group.getBase().getObjectByField(field));
	}

	@Test
	public void testGetBaseRarity() {
		subtestGetBase(MagicCardField.RARITY, Rarity.COMMON);
	}

	@Test
	public void testGetBaseArtist() {
		subtestGetBase(MagicCardField.ARTIST, "Name");
	}

	public void testGetBaseCost() {
		subtestGetBase(MagicCardField.COST, "{B}");
	}

	public void testGetBaseNumber() {
		subtestGetBase(MagicCardField.COLLNUM, "123");
	}

	public void testGetBaseNumber2() {
		subtestGetBase(MagicCardField.COST, "{B}");
		subtestGetBase(MagicCardField.COLLNUM, "123");
	}

	public void testGetBaseSPrice() {
		subtestGetBase(MagicCardField.DBPRICE, "1.2", 3 * 1.2f);
	}

	public void testGetBaseLang() {
		subtestGetBase(MagicCardField.LANG, "English");
	}

	@Test
	public void testGetCount() {
		assertEquals(cards.length, group.getCount());
	}

	@Test
	public void testGetOwnUSize() {
		assertEquals(cards.length, group.getCount());
	}

	@Test
	public void testSize() {
		assertEquals(cards.length, group.getCount());
	}

	@Test
	public void testRemove() {
		group.remove(cards[0]);
		assertEquals(cards.length - 1, group.getCount());
	}

	@Test
	public void testGetChildAtIndex() {
		for (int j = 0; j < cards.length; j++) {
			assertSame(cards[j], group.getChildAtIndex(j));
		}
	}

	@Test
	public void testGetLabelByField() {
		subtestGetBase(MagicCardField.RARITY, Rarity.COMMON);
		assertEquals(Rarity.COMMON, group.getLabelByField(MagicCardField.RARITY));
	}

	@Test
	public void testEqualsObject() {
		CardGroup old = group;
		group = new CardGroup(MagicCardField.RARITY, Rarity.COMMON);
		for (int j = 0; j < cards.length; j++) {
			group.add(cards[j]);
		}
		assertEquals(group, old);
	}

	@Test
	public void testRemoveEmptyChildren() {
		group.add(set);
		assertEquals(cards.length + 1, group.size());
		assertEquals(cards.length, group.getCount());
		group.removeEmptyChildren();
		assertEquals(cards.length, group.size());
	}

	@Test
	public void testSetProperty() {
		group.setProperty("aaa", cards);
		assertEquals(cards, group.getProperty("aaa"));
	}

	@Test
	public void testGetSubGroup() {
		group.add(set);
		assertSame(set, group.getSubGroup(set.getName()));
	}

	@Test
	public void testClear() {
		group.clear();
		assertEquals(0, group.getCount());
	}

	@Test
	public void testGetFirstCard() {
		assertSame(cards[0], group.getFirstCard());
	}

	@Test
	public void testGetFirstCardSub() {
		group.clear();
		group.add(set);
		IMagicCard card = generateCard();
		set.add(card);
		assertSame(card, group.getFirstCard());
	}

	@Test
	public void testContains() {
		for (int j = 0; j < cards.length; j++) {
			assertTrue(group.contains(cards[j]));
		}
	}

	@Test
	public void testExpandGroups() {
		group.add(set);
		set.add(generateCard());
		Collection<IMagicCard> result = group.expand();
		assertEquals(result.size(), group.getCount());
		assertEquals(result.size(), cards.length + 1);
	}
}
