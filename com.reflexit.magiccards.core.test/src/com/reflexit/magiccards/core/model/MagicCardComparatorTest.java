package com.reflexit.magiccards.core.model;

import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardModifiable;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.unittesting.CardGenerator;

public class MagicCardComparatorTest extends TestCase {
	private MagicCardComparator acc;
	private MagicCardComparator dec;
	private ICardField field;
	private IMagicCard card1;
	private IMagicCard card2;

	@Override
	@Before
	protected void setUp() throws Exception {
		field = MagicCardField.NAME;
		makeComparator(field);
		genMc();
	}

	public static void main(String[] args) {
		// check p/t
		HashMap<String, Integer> pmap = new HashMap<String, Integer>();
		IDbCardStore<IMagicCard> magicDBStore = DataManager.getInstance().getMagicDBStore();
		magicDBStore.initialize();
		for (IMagicCard card : magicDBStore) {
			String value = ((MagicCard) card).getCollNumber();
			if (value != null) {
				try {
					Integer.parseInt(value);
					value = "#";
				} catch (NumberFormatException e) {
					//
					if (value.endsWith("a")) {
						value = "#a";
					} else if (value.endsWith("b")) {
						value = "#b";
					} else {
						System.err.println(card);
					}
				}
			}
			Integer v = pmap.get(value);
			if (v == null) {
				v = 0;
			}
			v++;
			pmap.put(value, v);
		}
		for (String power : pmap.keySet()) {
			System.out.println(power + ": " + pmap.get(power));
		}
	}

	protected void makeComparator(ICardField field) {
		this.field = field;
		acc = new MagicCardComparator(field, true);
		dec = new MagicCardComparator(field, false);
	}

	public IMagicCard generateCard() {
		return CardGenerator.generateCardWithValues();
	}

	public MagicCardPhysical generatePhyCard() {
		return CardGenerator.generatePhysicalCardWithValues();
	}

	private void setField(IMagicCard y, ICardField field, Object value) {
		((ICardModifiable) y).set(field, value == null ? null : String.valueOf(value));
	}

	protected void genMc() {
		card1 = generateCard();
		card2 = generateCard();
	}

	protected void genMcp() {
		card1 = Mockito.spy(generatePhyCard());
		card2 = Mockito.spy(generatePhyCard());
	}

	public int sgn(int x) {
		if (x > 0)
			return 1;
		if (x < 0)
			return -1;
		return 0;
	}

	public void compareMcLess(ICardField field, Object a, Object b) {
		makeComparator(field);
		genMc();
		setField(card1, field, a);
		setField(card2, field, b);
		checkInvariantLessAndGroups(card1, card2);
		assertTrue(dec.compare(card1, card2) > 0);
		setField(card2, field, a);
		assertEquals(0, acc.compare(card1, card2));
	}

	public void compareMcEqual(ICardField field, Object a, Object b) {
		makeComparator(field);
		genMc();
		setField(card1, field, a);
		setField(card2, field, b);
		checkInvariantSame();
	}

	protected void checkInvariantSame() {
		assertEquals(0, acc.compare(card1, card2));
		assertEquals(0, acc.compare(card2, card1));
		assertEquals(0, dec.compare(card1, card2));
		assertEquals(0, dec.compare(card2, card1));
	}

	protected void checkInvariantLess(IMagicCard card1, IMagicCard card2) {
		assertEquals(-1, sgn(acc.compare(card1, card2)));
		assertEquals(1, sgn(acc.compare(card2, card1)));
		assertEquals(0, acc.compare(card1, card1));
		assertEquals(0, acc.compare(card1, card1.cloneCard()));
	}

	protected void checkInvariantLessAndGroups(IMagicCard card1, IMagicCard card2) {
		checkInvariantLess(card1, card2);
		CardGroup g1 = new CardGroup(field, String.valueOf(card1.get(field)));
		g1.add(card1);
		g1.add(card1.cloneCard());
		CardGroup g2 = new CardGroup(field, String.valueOf(card2.get(field)));
		g2.add(card2);
		g2.add(card2.cloneCard());
		assertEquals(-1, sgn(acc.compare(g1, g2)));
	}

	@Test
	public void testName() {
		compareMcLess(MagicCardField.NAME, "a", "b");
	}

	@Test
	public void testCl() {
		card1 = generateCard();
		card2 = generatePhyCard();
		checkInvariantLessAndGroups(card1, card2);
	}

	public void testNameSame() {
		String same = "a";
		setField(card1, field, same);
		setField(card2, field, same);
		checkInvariantSame();
	}

	// char c[] = { 'W', 'U', 'B', 'R', 'G' };
	public void testColor1() {
		compareMcLess(MagicCardField.COST, "{W}", "{B}");
		compareMcLess(MagicCardField.COST, "{W}", "{U}");
		compareMcLess(MagicCardField.COST, "{W}", "{R}");
		compareMcLess(MagicCardField.COST, "{W}", "{G}");
		compareMcLess(MagicCardField.COST, "{B}", "{G}");
		compareMcLess(MagicCardField.COST, "{R}", "{G}");
		compareMcLess(MagicCardField.COST, "{U}", "{R}");
		compareMcEqual(MagicCardField.COST, "{W}", "{W}");
		compareMcLess(MagicCardField.COST, "{R}", "{W}{R}");
		compareMcLess(MagicCardField.COST, "{W}", "{W}{R}");
	}

	public void testColor2() {
		compareMcEqual(MagicCardField.COST, "{W}{W}", "{W}");
	}

	public void testColorNull() {
		compareMcLess(MagicCardField.COST, "{W}", "");
		compareMcLess(MagicCardField.COST, "{G}", "");
		compareMcEqual(MagicCardField.COST, "", "");
	}

	public void testColorNullSt() {
		try {
			compareMcEqual(MagicCardField.COST, "", null);
			fail("Expected to throw NPE");
		} catch (NullPointerException e) {
			// good
		}
	}

	public void testPower() {
		compareMcLess(MagicCardField.POWER, "1", "2");
		compareMcLess(MagicCardField.POWER, "3", "11");
		compareMcLess(MagicCardField.TOUGHNESS, "1", "2");
		compareMcLess(MagicCardField.TOUGHNESS, "3", "5");
	}

	public void testPowerStar() {
		compareMcLess(MagicCardField.POWER, "*", "1");
		compareMcEqual(MagicCardField.POWER, "*", "1+*");
		compareMcEqual(MagicCardField.POWER, "1+*", "2+*");
		compareMcEqual(MagicCardField.POWER, "1{1/2}", "2{1/2}");
		compareMcEqual(MagicCardField.POWER, "*", "2{1/2}");
	}

	public void testRarity() {
		compareMcLess(MagicCardField.RARITY, Rarity.OTHER, Rarity.LAND);
		compareMcLess(MagicCardField.RARITY, Rarity.LAND, Rarity.COMMON);
		compareMcLess(MagicCardField.RARITY, Rarity.COMMON, Rarity.UNCOMMON);
		compareMcLess(MagicCardField.RARITY, Rarity.UNCOMMON, Rarity.RARE);
		compareMcLess(MagicCardField.RARITY, Rarity.RARE, Rarity.MYTHIC_RARE);
		compareMcLess(MagicCardField.RARITY, Rarity.SPECIAL, Rarity.LAND);
	}

	public void testCollNum() {
		compareMcLess(MagicCardField.COLLNUM, "1", "2");
		compareMcLess(MagicCardField.COLLNUM, "1", "10");
		compareMcLess(MagicCardField.COLLNUM, "1", "1a");
		compareMcLess(MagicCardField.COLLNUM, "10", "10a");
		compareMcLess(MagicCardField.COLLNUM, "10a", "10b");
		compareMcLess(MagicCardField.COLLNUM, "", "1");
		compareMcEqual(MagicCardField.COLLNUM, null, "");
		compareMcLess(MagicCardField.COLLNUM, "1a", "10a");
	}

	@Test
	public void testOr() {
		compareMcLess(MagicCardField.ORACLE, "", null);
		compareMcLess(MagicCardField.ORACLE, "", "a");
		compareMcLess(MagicCardField.ORACLE, "a", "b");
		compareMcLess(MagicCardField.ORACLE, "a", "aa");
	}

	public void testCMC() {
		makeComparator(MagicCardField.CMC);
		setField(card1, MagicCardField.COST, "{B}");
		setField(card2, MagicCardField.COST, "{3}{W}");
		checkInvariantLessAndGroups(card1, card2);
		setField(card1, MagicCardField.COST, "{W}");
		setField(card2, MagicCardField.COST, "{W}");
		checkInvariantSame();
	}

	public void testCMC_COST() {
		makeComparator(MagicCardField.CMC);
		setField(card1, MagicCardField.COST, "{W}");
		setField(card2, MagicCardField.COST, "{B}");
		checkInvariantLessAndGroups(card1, card2);
	}

	public void testCMC_Land() {
		makeComparator(MagicCardField.CMC);
		setField(card1, MagicCardField.COST, "{0}");
		setField(card2, MagicCardField.COST, "");
		setField(card2, MagicCardField.NAME, "Forest");
		setField(card2, MagicCardField.TYPE, "Basic Land");
		checkInvariantLessAndGroups(card2, card1);
	}

	public void testCMC_Land2() {
		makeComparator(MagicCardField.CMC);
		setField(card1, MagicCardField.COST, "");
		setField(card1, MagicCardField.NAME, "Forest");
		setField(card1, MagicCardField.TYPE, "Basic Land");
		setField(card2, MagicCardField.COST, "");
		setField(card2, MagicCardField.NAME, "Something");
		setField(card2, MagicCardField.TYPE, "Some Type");
		checkInvariantLessAndGroups(card1, card2);
	}

	@Test
	public void testCount() {
		makeComparator(MagicCardField.COUNT);
		genMcp();
		setField(card1, field, "1");
		setField(card2, field, "2");
		checkInvariantLessAndGroups(card1, card2);
		setField(card2, field, "10");
		checkInvariantLessAndGroups(card1, card2);
	}

	public void testOwnCount() {
		makeComparator(MagicCardField.OWN_COUNT);
		genMcp();
		((MagicCardPhysical) card1).setOwn(true);
		((MagicCardPhysical) card2).setOwn(true);
		setField(card1, MagicCardField.COUNT, "1");
		setField(card2, MagicCardField.COUNT, "2");
		checkInvariantLessAndGroups(card1, card2);
		setField(card2, MagicCardField.COUNT, "10");
		checkInvariantLessAndGroups(card1, card2);
		setField(card1, MagicCardField.COUNT, "2");
		setField(card2, MagicCardField.COUNT, "2");
		checkInvariantSame();
		when(((MagicCardPhysical) card2).getOwnTotal()).thenReturn(3);
		checkInvariantLess(card1, card2);
	}
}
