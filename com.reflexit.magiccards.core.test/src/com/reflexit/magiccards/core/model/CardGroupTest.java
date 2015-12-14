package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.junit.Test;
import org.mockito.Mockito;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.abs.ICardModifiable;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.unittesting.CardGenerator;

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

	public IMagicCard[] populateGroup(ICardField field, Object value, Object expected, int len, boolean phy) {
		group = new CardGroup(field, String.valueOf(expected));
		IMagicCard[] res = new IMagicCard[len];
		for (int i = 0; i < len; i++) {
			IMagicCard card = phy ? generatePhyCard() : generateCard();
			((ICardModifiable) card).set(field, value);
			group.add(card);
			res[i] = card;
		}
		return res;
	}

	public void setCardsInGroup(ICardField field, Object value) {
		for (Iterator iterator = group.iterator(); iterator.hasNext();) {
			IMagicCardPhysical card = (IMagicCardPhysical) iterator.next();
			((ICardModifiable) card).set(field, value);
		}
	}

	public IMagicCard generateCard() {
		return CardGenerator.generateCardWithValues();
	}

	public MagicCardPhysical generatePhyCard() {
		MagicCardPhysical card = CardGenerator.generatePhysicalCardWithValues();
		card.setCount(1);
		card.setOwn(true);
		return card;
	}

	public void groupAndTest(ICardField field, Object expected) {
		populateGroup(field, String.valueOf(expected), expected, 3, true);
		assertEquals(expected, group.get(field));
	}

	public void groupAndTest(ICardField field, Object value, Object expected) {
		populateGroup(field, value, expected, 3, true);
		assertEquals(expected, group.get(field));
	}

	@Test
	public void testGetBaseRarity() {
		groupAndTest(MagicCardField.RARITY, Rarity.COMMON);
	}

	@Test
	public void testGetBaseArtist() {
		groupAndTest(MagicCardField.ARTIST, "Name");
	}

	public void testGetBaseCost() {
		groupAndTest(MagicCardField.COST, "{B}");
	}

	public void testGetBaseNumber() {
		groupAndTest(MagicCardField.COLLNUM, "123");
	}

	public void testGetBaseNumber2() {
		groupAndTest(MagicCardField.COST, "{B}");
		groupAndTest(MagicCardField.COLLNUM, "123");
	}

	public void testGetBaseSPrice() {
		groupAndTest(MagicCardField.DBPRICE, "1.2", 3 * 1.2f);
	}

	public void testGetBaseLang() {
		groupAndTest(MagicCardField.LANG, "English", null);
		groupAndTest(MagicCardField.LANG, "German");
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
		((MagicCardPhysical) cards[1]).setCount(1);
		((MagicCardPhysical) cards[2]).setCount(4);
		assertEquals(2, group.getOwnUnique());
		int c = (Integer) MagicCardField.OWN_UNIQUE.aggregateValueOf(group);
		assertEquals(2, c);
		c = (Integer) MagicCardField.OWN_COUNT.aggregateValueOf(group);
		assertEquals(5, c);
		c = (Integer) MagicCardField.FORTRADECOUNT.aggregateValueOf(group);
		assertEquals(0, c);
	}

	@Test
	public void testGetOwnUSizeForTrade() {
		cards = populateGroup(group, 3, true);
		((MagicCardPhysical) cards[0]).setOwn(false);
		((MagicCardPhysical) cards[1]).setCount(1);
		((MagicCardPhysical) cards[2]).setCount(2);
		((MagicCardPhysical) cards[2]).setSpecialTag(MagicCardField.FORTRADECOUNT);
		assertEquals(2, group.getOwnUnique());
		int c = (Integer) MagicCardField.OWN_UNIQUE.aggregateValueOf(group);
		assertEquals(2, c);
		c = (Integer) MagicCardField.OWN_COUNT.aggregateValueOf(group);
		assertEquals(3, c);
		c = (Integer) MagicCardField.FORTRADECOUNT.aggregateValueOf(group);
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
		groupAndTest(MagicCardField.RARITY, Rarity.COMMON);
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
		group.setFilter(new MagicCardFilter());
		assertEquals(cards.length, group.size());
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
		groupAndTest(MagicCardField.OWNERSHIP, true);
		assertEquals(true, group.isOwn());
	}

	@Test
	public void testLocation() {
		Location loc = Location.valueOf("xxx");
		groupAndTest(MagicCardField.LOCATION, loc);
		assertEquals(loc, group.getLocation());
		group = new CardGroup(MagicCardField.SIDEBOARD, "false");
		populateGroup(group, 3, true);
		setCardsInGroup(MagicCardField.LOCATION, loc);
		assertEquals(false, group.get(MagicCardField.SIDEBOARD));
	}

	@Test
	public void testSideboard() {
		Location loc = Location.valueOf("xxx").toSideboard();
		groupAndTest(MagicCardField.LOCATION, loc);
		group = new CardGroup(MagicCardField.SIDEBOARD, "true");
		populateGroup(group, 3, true);
		setCardsInGroup(MagicCardField.LOCATION, loc);
		assertEquals(true, group.get(MagicCardField.SIDEBOARD));
		assertEquals(true, group.isSideboard());
	}

	@Test
	public void testPhyCount() {
		groupAndTest(MagicCardField.COUNT, 1, 3);
		assertEquals(3, group.getCount());
	}

	@Test
	public void testPrice() {
		groupAndTest(MagicCardField.COUNT, 1, 3);
		groupAndTest(MagicCardField.PRICE, 1.0f, 3.0f);
	}

	@Test
	public void testComment() {
		String comment = "tapochki";
		groupAndTest(MagicCardField.COMMENT, comment);
		assertEquals(comment, group.getComment());
	}

	@Test
	public void testCustom() {
		String comment = "a,b";
		groupAndTest(MagicCardField.CUSTOM, comment);
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

	@Test
	public void testContractOne() {
		group = new CardGroup(MagicCardField.NAME, "My Name");
		MagicCardPhysical card = generatePhyCard();
		preset1(card);
		group.add(card);
		ICardField[] allFields = MagicCardField.allFields();
		checkConsistency(MagicCardField.NAME, group.getName(), "My Name");
		for (ICardField field : allFields) {
			if (field == MagicCardField.NAME)
				continue;
			assertEquals("Failed for " + field, card.get(field), group.get(field));
		}
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
		group.recache();
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
		checkConsistency(MagicCardField.LEGALITY);
		checkConsistency(MagicCardField.FLIPID, group.getFlipId());
		checkConsistency(MagicCardField.PART);
		checkConsistency(MagicCardField.OTHER_PART);
		checkConsistency(MagicCardField.SET_BLOCK);
		checkConsistency(MagicCardField.SET_CORE);
		checkConsistency(MagicCardField.POWER, group.getPower(), String.valueOf(1.0f * count));
		checkConsistency(MagicCardField.TOUGHNESS, group.getToughness(), String.valueOf(1.0f * count));
	}

	public void testLegalityAggr() {
		group = new CardGroup(MagicCardField.NAME, Rarity.COMMON);
		cards = new IMagicCard[3];
		for (int j = 0; j < cards.length; j++) {
			MagicCard c = (MagicCard) generateCard();
			c.setLegalityMap(LegalityMap.createFromLegal("Standard"));
			cards[j] = c;
			group.add(c);
		}
		group.recache();
		checkConsistency(MagicCardField.LEGALITY);
	}

	public void preset1(MagicCardPhysical card) {
		card.setCount(1);
		card.setSpecial("foil");
		card.setDbPrice(1.0f);
		card.set(MagicCardField.RATING, "1");
		card.set(MagicCardField.TOUGHNESS, "1.0");
		card.set(MagicCardField.POWER, "1.0");
		card.set(MagicCardField.SET, "Lorwyn");
		card.set(MagicCardField.TYPE, "Creature - Elf");
		card.setOwn(true);
	}

	public void preset2(MagicCardPhysical card) {
		card.setCount(1);
		card.setSpecial("foil");
		card.setDbPrice(1.0f);
		card.set(MagicCardField.RATING, "1");
		card.set(MagicCardField.TOUGHNESS, null);
		card.set(MagicCardField.POWER, null);
		card.set(MagicCardField.SET, "Lorwyn");
		card.set(MagicCardField.TYPE, "Instant");
	}

	public void checkConsistency(ICardField field) {
		IMagicCard card = group.getFirstCard();
		Object c1 = card.get(field);
		Object g1 = group.get(field);
		assertEquals(c1, g1);
	}

	public void checkConsistency(ICardField field, Object value) {
		MagicCardPhysical card = (MagicCardPhysical) group.getFirstCard();
		assertNotNull(card);
		assertEquals("For field " + field, card.get(field), value);
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
			System.err.println(o.getCardId() + ": " + o.getName() + " x " + o.getCount() + " $"
					+ o.getDbPrice());
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
		}
		((ICardModifiable) cards[0]).set(MagicCardField.TYPE, "Creature - Elf");
		((ICardModifiable) cards[1]).set(MagicCardField.TYPE, "Instant");
		((ICardModifiable) cards[2]).set(MagicCardField.TYPE, "Sorcery");
		ArrayList<IMagicCard> acards = new ArrayList<IMagicCard>(Arrays.asList(cards));
		group = CardStoreUtils.buildTypeGroups(acards);
		group.setFilter(new MagicCardFilter());
		//	group.removeEmptyChildren();
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

	public void testPowerAggr() {
		group = new CardGroup(MagicCardField.POWER, "power");
		for (int j = 0; j < cards.length; j++) {
			cards[j] = CardGenerator.generatePhysicalCardWithValues();
		}
		preset2((MagicCardPhysical) cards[0]);
		preset1((MagicCardPhysical) cards[1]);
		preset1((MagicCardPhysical) cards[2]);
		group.add(cards[0]);
		group.add(cards[1]);
		group.add(cards[2]);
		assertEquals("2.0", group.getPower());
		assertEquals(2, group.getInt(MagicCardField.CREATURE_COUNT));
	}

	public void testPowerAggrMC() {
		group = new CardGroup(MagicCardField.POWER, "power");
		for (int j = 0; j < cards.length; j++) {
			cards[j] = CardGenerator.generateRandomCard();
			MagicCard card = (MagicCard) cards[j];
			card.setName("name " + j);
		}
		((MagicCard) cards[0]).setPower("1");
		((MagicCard) cards[1]).setPower("2");
		group.add(cards[0]);
		group.add(cards[1]);
		group.add(cards[2]);
		assertEquals("3.0", group.getPower());
		assertEquals(2, group.getInt(MagicCardField.CREATURE_COUNT));
	}
}
