package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.junit.Test;
import org.mockito.Mockito;

import com.reflexit.magiccards.core.model.utils.CardGenerator;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;

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

	public void populateGroup(CardGroup g, ICard[] some) {
		g.clear();
		for (int i = 0; i < some.length; i++) {
			ICard object = some[i];
			g.add(object);
		}
	}

	public IMagicCard[] populateGroup(CardGroup g, int len, boolean phy) {
		g.clear();
		IMagicCard[] res = new IMagicCard[len];
		for (int i = 0; i < len; i++) {
			IMagicCard object = phy ? generatePhyCard() : generateCard();
			g.add(object);
			res[i] = object;
		}
		return res;
	}

	public IMagicCard generateCard() {
		return CardGenerator.generateCardWithValues();
	}

	public MagicCardPhysical generatePhyCard() {
		return CardGenerator.generatePhysicalCardWithValues();
	}

	public void subtestGetBase(ICardField field, Object expected) {
		subtestGetBase(field, String.valueOf(expected), expected);
	}

	public void subtestGetBase(ICardField field, Object value, Object expected) {
		for (Iterator iterator = group.iterator(); iterator.hasNext();) {
			IMagicCardPhysical card = (IMagicCardPhysical) iterator.next();
			((ICardModifiable) card).set(field, String.valueOf(value));
		}
		group.rehash();
		assertEquals(expected, group.get(field));
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
		subtestGetBase(MagicCardField.LANG, "English", null);
		subtestGetBase(MagicCardField.LANG, "German");
	}

	@Test
	public void testGetCount() {
		assertEquals(group.size(), group.getCount());
	}

	@Test
	public void testUniqueCount() {
		assertEquals(cards.length, group.getUniqueCount());
	}

	@Test
	public void testGetOwnUSize() {
		assertEquals(0, group.getOwnUnique());
	}

	@Test
	public void testGetOwnUSizePhy() {
		populateGroup(group, 3, true);
		assertEquals(3, group.getOwnUnique());
	}

	@Test
	public void testGetOwnUSizePhy2() {
		cards = populateGroup(group, 3, true);
		((MagicCardPhysical) cards[0]).setOwn(false);
		((MagicCardPhysical) cards[2]).setCount(4);
		((MagicCardPhysical) cards[2]).setForTrade(2);
		assertEquals(2, group.getOwnUnique());
		int c = (Integer) MagicCardField.OWN_UNIQUE.valueOf(group);
		assertEquals(2, c);
		c = (Integer) MagicCardField.OWN_COUNT.valueOf(group);
		assertEquals(5, c);
		c = (Integer) MagicCardField.FORTRADECOUNT.valueOf(group);
		assertEquals(2, c);
	}

	@Test
	public void testSize() {
		assertEquals(cards.length, group.size());
	}

	@Test
	public void testRemove() {
		group.remove(cards[0]);
		assertEquals(cards.length - 1, group.getUniqueCount());
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
		ICardGroup old = group;
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
		assertEquals(cards.length, group.getUniqueCount());
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
		assertEquals(result.size(), group.getUniqueCount());
		assertEquals(result.size(), cards.length + 1);
	}

	@Test
	public void testOwnership() {
		populateGroup(group, 3, true);
		subtestGetBase(MagicCardField.OWNERSHIP, true);
		assertEquals(true, group.isOwn());
	}

	@Test
	public void testLocation() {
		populateGroup(group, 3, true);
		Location loc = Location.valueOf("xxx");
		subtestGetBase(MagicCardField.LOCATION, loc);
		assertEquals(loc, group.getLocation());
		subtestGetBase(MagicCardField.SIDEBOARD, false);
	}

	@Test
	public void testPhyCount() {
		populateGroup(group, 3, true);
		subtestGetBase(MagicCardField.COUNT, 1, 3);
		assertEquals(3, group.getCount());
	}

	@Test
	public void testPrice() {
		populateGroup(group, 3, true);
		subtestGetBase(MagicCardField.COUNT, 1, 3);
		subtestGetBase(MagicCardField.PRICE, 1.0f, 3.0f);
	}

	@Test
	public void testComment() {
		populateGroup(group, 3, true);
		String comment = "tapochki";
		subtestGetBase(MagicCardField.COMMENT, comment);
		assertEquals(comment, group.getComment());
	}

	@Test
	public void testCustom() {
		populateGroup(group, 3, true);
		String comment = "a,b";
		subtestGetBase(MagicCardField.CUSTOM, comment);
	}

	@Test
	public void testSideboard() {
		populateGroup(group, 3, true);
		Location loc = Location.valueOf("xxx").toSideboard();
		subtestGetBase(MagicCardField.LOCATION, loc);
		subtestGetBase(MagicCardField.SIDEBOARD, true);
		assertEquals(true, group.isSideboard());
	}

	@Test
	public void testOwnCount() {
		MagicCardPhysical card = generatePhyCard();
		card.setCount(2);
		card.setOwn(true);
		for (int j = 0; j < cards.length; j++) {
			cards[j] = card.cloneCard();
			group.add(cards[j]);
		}
		((MagicCardPhysical) cards[0]).setOwn(false);
		populateGroup(group, cards);
		assertEquals(true, group.isOwn());
		assertEquals(4, group.getOwnCount());
		assertEquals(1, group.getOwnUnique());
		assertEquals(6, group.getCount());
	}

	@Test
	public void testContract() {
		group = new CardGroup(MagicCardField.NAME, "My Name");
		MagicCardPhysical card = generatePhyCard();
		preset1(card);
		for (int j = 0; j < cards.length; j++) {
			cards[j] = card.cloneCard();
			group.add(cards[j]);
		}
		populateGroup(group, cards);
		checkConsistency(MagicCardField.NAME, group.getName(), "My Name");
		checkAllConsistency();
	}

	public void checkAllConsistency() {
		boolean same = true;
		int count = 0;
		for (int j = 0; j < cards.length; j++) {
			if (cards[0].getCardId() != cards[j].getCardId()) {
				same = false;
			}
			count += ((IMagicCardPhysical) cards[j]).getCount();
		}
		group.rehash();
		checkConsistency(MagicCardField.OWNERSHIP, group.isOwn());
		checkConsistency(MagicCardField.OWN_COUNT, group.getOwnCount(), count);
		checkConsistency(MagicCardField.OWN_UNIQUE, group.getOwnUnique(), same ? 1 : count);
		checkConsistency(MagicCardField.COUNT, group.getCount(), count);
		checkConsistency(MagicCardField.FORTRADECOUNT, group.getForTrade(), 0);
		checkConsistency(MagicCardField.SPECIAL, group.getSpecial());
		int uniqueCount = group.getUniqueCount();
		checkConsistency(MagicCardField.UNIQUE_COUNT, uniqueCount, same ? 1 : count);
		if (same) {
			checkConsistency(MagicCardField.COMMENT, group.getComment());
			checkConsistency(MagicCardField.COST, group.getCost());
			checkConsistency(MagicCardField.TYPE, group.getType());
			checkConsistency(MagicCardField.ORACLE, group.getOracleText());
			checkConsistency(MagicCardField.TEXT, group.getText());
			checkConsistency(MagicCardField.CMC, group.getCmc());
			checkConsistency(MagicCardField.ARTIST, group.getArtist());
			checkConsistency(MagicCardField.COLLNUM);
			checkConsistency(MagicCardField.ID, group.getCardId());
		}
		checkConsistency(MagicCardField.SET, group.getSet());
		checkConsistency(MagicCardField.RARITY, group.getRarity());
		checkConsistency(MagicCardField.CTYPE, group.getColorType());
		checkConsistency(MagicCardField.DBPRICE, group.getDbPrice(), 1.0f * count);
		checkConsistency(MagicCardField.LANG, group.getLanguage());
		checkConsistency(MagicCardField.EDITION_ABBR);
		checkConsistency(MagicCardField.RATING, 1.0f * count, 1.0f * count);
		checkConsistency(MagicCardField.RULINGS, group.getRulings());
		checkConsistency(MagicCardField.ENID, group.getEnglishCardId());
		checkConsistency(MagicCardField.PROPERTIES);
		checkConsistency(MagicCardField.FLIPID, group.getFlipId());
		checkConsistency(MagicCardField.PART);
		checkConsistency(MagicCardField.OTHER_PART);
		checkConsistency(MagicCardField.SET_BLOCK);
		checkConsistency(MagicCardField.SET_CORE);
		checkConsistency(MagicCardField.POWER, group.getPower(), String.valueOf(1.0f * count));
		checkConsistency(MagicCardField.TOUGHNESS, group.getToughness(), String.valueOf(1.0f * count));
	}

	public void preset1(MagicCardPhysical card) {
		card.setCount(1);
		card.setSpecial("foil");
		card.setDbPrice(1.0f);
		card.set(MagicCardField.RATING, "1");
		card.set(MagicCardField.TOUGHNESS, "1.0");
		card.set(MagicCardField.POWER, "1.0");
		card.set(MagicCardField.SET, "Lorwyn");
	}

	public void checkConsistency(ICardField field) {
		MagicCardPhysical card = (MagicCardPhysical) group.getFirstCard();
		assertEquals(card.get(field), group.get(field));
	}

	public void checkConsistency(ICardField field, Object value) {
		MagicCardPhysical card = (MagicCardPhysical) group.getFirstCard();
		assertNotNull(card);
		assertEquals(card.get(field), value);
		assertEquals(value, group.get(field));
	}

	public void checkConsistency(ICardField field, Object value, Object compare) {
		assertEquals("Failed compare(1) for " + field, compare, value);
		assertEquals("Failed compare(2) for " + field, value, group.get(field));
	}

	public void printTree(CardGroup g, int level) {
		for (Iterator iterator = g.iterator(); iterator.hasNext();) {
			IMagicCardPhysical o = (IMagicCardPhysical) iterator.next();
			for (int i = 0; i < level; i++) {
				System.err.print("--");
			}
			System.err.println(o.getCardId() + ": " + o.getName() + " x " + o.getCount() + " $" + o.getDbPrice());
			if (o instanceof ICardGroup) {
				printTree((CardGroup) o, level + 1);
			}
		}
	}

	@Test
	public void testLeyers() {
		for (int j = 0; j < cards.length; j++) {
			cards[j] = generatePhyCard();
			preset1((MagicCardPhysical) cards[j]);
			((ICardModifiable) cards[j]).set(MagicCardField.COUNT, "1");
		}
		((ICardModifiable) cards[0]).set(MagicCardField.TYPE, "Creature - Elf");
		((ICardModifiable) cards[1]).set(MagicCardField.TYPE, "Instant");
		((ICardModifiable) cards[2]).set(MagicCardField.TYPE, "Sorcery");
		ArrayList<IMagicCard> acards = new ArrayList<IMagicCard>(Arrays.asList(cards));
		group = CardStoreUtils.buildTypeGroups(acards);
		group.removeEmptyChildren();
		// printTree(group, 0);
		// checkConsistency(MagicCardField.POWER, group.getPower(), "6.0");
		checkAllConsistency();
	}

	public void testNameGroup() {
		MagicCard m = Mockito.spy((MagicCard) generateCard());
		group = new CardGroup(MagicCardField.NAME, m.getName());
		CardGroup realcards = new CardGroup(MagicCardField.ID, m.getName());
		Mockito.when(m.getRealCards()).thenReturn(realcards);
		Location loc = Location.createLocation("xxx");
		for (int j = 0; j < cards.length; j++) {
			cards[j] = CardGenerator.generatePhysicalCardWithValues(m);
			MagicCardPhysical mcp = (MagicCardPhysical) cards[j];
			mcp.setLocation(loc);
			mcp.setOwn(true);
			mcp.setCount(1);
			realcards.add(cards[j]);
		}
		group.add(m);
		assertEquals(loc, group.getLocation());
		assertEquals(true, group.isOwn());
	}

	public void testNameGroupPhy() {
		MagicCard m = (MagicCard) generateCard();
		group = new CardGroup(MagicCardField.NAME, m.getName());
		Location loc = Location.createLocation("xxx");
		for (int j = 0; j < cards.length; j++) {
			cards[j] = CardGenerator.generatePhysicalCardWithValues(m);
			MagicCardPhysical mcp = (MagicCardPhysical) cards[j];
			mcp.setLocation(loc);
			mcp.setOwn(true);
			mcp.setCount(1);
			assertTrue(mcp.isOwn());
		}
		group.add(cards[0]);
		assertEquals(loc, group.getLocation());
		assertEquals(true, group.isOwn());
		group.add(cards[1]);
		assertEquals(loc, group.getLocation());
		assertEquals(true, group.isOwn());
	}
}
