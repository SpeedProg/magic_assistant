package com.reflexit.magiccards.core.model;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.unittesting.CardGenerator;

public class SortOrderTest extends TestCase {
	private IMagicCard theCard;
	private IMagicCard[] cards;
	private SortOrder order;

	public MagicCardPhysical preset1(MagicCardPhysical card) {
		card.setCount(1);
		card.setSpecialTag("foil");
		card.setDbPrice(1.0f);
		card.set(MagicCardField.RATING, "1");
		card.set(MagicCardField.TOUGHNESS, "1.0");
		card.set(MagicCardField.POWER, "1.0");
		card.set(MagicCardField.SET, "Lorwyn");
		return card;
	}

	public IMagicCard generateCard() {
		return CardGenerator.generateCardWithValues();
	}

	public MagicCardPhysical generatePhyCard() {
		return CardGenerator.generatePhysicalCardWithValues();
	}

	private void setField(IMagicCard y, ICardField field, Object value) {
		((MagicCardPhysical) y).set(field, String.valueOf(value));
	}

	public IMagicCard cloneOne(IMagicCard x, ICardField field, Object value) {
		IMagicCard y = cloneFull(x);
		setField(y, field, value);
		return y;
	}

	public IMagicCard cloneFull(IMagicCard x) {
		IMagicCard y = x.cloneCard();
		((MagicCardPhysical) y).setMagicCard(x.getBase().cloneCard());
		return y;
	}

	@Override
	public void setUp() {
		theCard = preset1(generatePhyCard());
		order = new SortOrder();
	}

	private int sign(int compare) {
		if (compare == 0)
			return 0;
		if (compare > 0)
			return 1;
		return -1;
	}

	public void contractTest(IMagicCard x, IMagicCard y, IMagicCard z) {
		assertEquals(-1, sign(order.compare(x, y)));
		assertEquals(-1, sign(order.compare(y, z)));
		assertEquals(-1, sign(order.compare(x, z)));
		assertEquals(1, sign(order.compare(y, x)));
		assertEquals(1, sign(order.compare(z, y)));
		assertEquals(1, sign(order.compare(z, x)));
		assertEquals(0, sign(order.compare(x, x)));
		IMagicCard z1 = cloneFull(z);
		assertEquals(z, z1);
		assertTrue(order.compare(z, z1) != 0);
		assertEquals(-1, sign(order.compare(x, z1)));
	}

	public void cloneAndSet(ICardField field, Object... args) {
		int i = 0;
		cards = new MagicCardPhysical[args.length];
		for (Object object : args) {
			cards[i] = cloneOne(theCard, field, object);
			i++;
		}
	}

	public void setFieldMass(ICardField field, Object... args) {
		int i = 0;
		for (Object object : args) {
			setField(cards[i], field, object);
			i++;
		}
	}

	@Test
	public void testIdEmpty() {
		cloneAndSet(MagicCardField.ID, 1, 2, 3);
		contractTest3();
	}

	public void contractTest3() {
		contractTest(cards[0], cards[1], cards[2]);
	}

	@Test
	public void testNameEmpty() {
		setField(theCard, MagicCardField.ID, 0);
		cloneAndSet(MagicCardField.NAME, "a", "b", "c");
		contractTest3();
	}

	@Test
	public void testNameField() {
		order.setSortField(MagicCardField.NAME, true);
		cloneAndSet(MagicCardField.NAME, "a", "b", "c");
		contractTest3();
	}

	@Test
	public void testNameFieldRev() {
		order.setSortField(MagicCardField.NAME, false);
		cloneAndSet(MagicCardField.NAME, "c", "b", "a");
		contractTest3();
	}

	@Test
	public void testNameFieldRev2() {
		order.setSortField(MagicCardField.NAME, true);
		order.setSortField(MagicCardField.NAME, false);
		cloneAndSet(MagicCardField.NAME, "c", "b", "a");
		contractTest3();
	}

	@Test
	public void testFieldPow() {
		order.setSortField(MagicCardField.POWER, true);
		cloneAndSet(MagicCardField.POWER, 1, 2, 3);
		contractTest3();
	}

	public void testFieldSpec() {
		order.setSortField(MagicCardField.SPECIAL, true);
		cloneAndSet(MagicCardField.SPECIAL, "a", "b", "c");
		contractTest3();
	}

	public void testFieldSpec2() {
		order.setSortField(MagicCardField.NAME, true);
		order.setSortField(MagicCardField.SPECIAL, true);
		order.setSortField(MagicCardField.CMC, true);
		order.setSortField(MagicCardField.COST, true);
		order.setSortField(MagicCardField.TYPE, true);
		cloneAndSet(MagicCardField.SPECIAL, "a", "b", "c");
		setFieldMass(MagicCardField.NAME, "c", "b", "a");
		contractTest3();
	}

	public void testFieldSpecGone() {
		order.setSortField(MagicCardField.NAME, true);
		order.setSortField(MagicCardField.SPECIAL, true);
		order.setSortField(MagicCardField.TOUGHNESS, true);
		order.setSortField(MagicCardField.POWER, true);
		order.setSortField(MagicCardField.CMC, true);
		order.setSortField(MagicCardField.COST, true);
		order.setSortField(MagicCardField.TYPE, true);
		order.setSortField(MagicCardField.TEXT, true);
		order.setSortField(MagicCardField.ARTIST, true);
		assertEquals(MagicCardField.ARTIST, order.peek().getField());
		assertEquals(9, order.size());
		cloneAndSet(MagicCardField.SPECIAL, "a", "b", "c");
		setFieldMass(MagicCardField.NAME, "c", "b", "a");
		contractTest(cards[2], cards[1], cards[0]);
	}
}
