/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.CardTypes;
import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.utils.CardGenerator;
import com.reflexit.magiccards.core.test.assist.MemCardHandler;

/**
 * @author Alena
 * 
 */
public class AbstractFilteredCardStoreTest extends TestCase {
	private static final String INSTANT = "Instant";
	private static final String ARTIFACT = "Artifact";
	private static final String RED_COST = "{R}";
	private static final String BLACK_COST = "{B}";
	private static final String WHITE_COST = "{W}";
	private static final String ONE_COST = "{1}";
	MemCardHandler deck;
	MagicCardFilter filter;
	private MagicCard card1;
	private MagicCard card2;
	private MagicCard card3;

	@Override
	protected void setUp() throws Exception {
		this.deck = new MemCardHandler();
		this.filter = this.deck.getFilter();
	}

	private void add3cards() {
		card1 = CardGenerator.generateRandomCard();
		card1.setCost(BLACK_COST);
		card1.setType("Creature - Elf");
		card2 = CardGenerator.generateRandomCard();
		card2.setCost(RED_COST);
		card2.setType("Creature - Goblin");
		card3 = CardGenerator.generateRandomCard();
		card3.setCost(WHITE_COST);
		card3.setType(INSTANT);
		this.deck.getCardStore().add(card1);
		this.deck.getCardStore().add(card2);
		this.deck.getCardStore().add(card3);
	}

	private MagicCard addCard(String color, String type, String name) {
		MagicCard card;
		card = CardGenerator.generateRandomCard();
		card.setCost(color);
		card.setType(type);
		card.setName(name);
		this.deck.getCardStore().add(card);
		return card;
	}

	/**
	 * Test method for
	 * {@link com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore#update(com.reflexit.magiccards.core.model.MagicCardFilter)}
	 * .
	 */
	@Test
	public void testGetCardGrpups() {
		add3cards();
		this.filter.setSortField(MagicCardField.COST, true);
		this.filter.setGroupField(MagicCardField.COST);
		this.deck.update();
		Object[] cardGroups = this.deck.getCardGroupRoot().getChildren();
		assertEquals(3, cardGroups.length);
		assertEquals("White", ((CardGroup) cardGroups[0]).getName());
		assertEquals("Black", ((CardGroup) cardGroups[1]).getName());
		assertEquals("Red", ((CardGroup) cardGroups[2]).getName());
	}

	protected MagicCard[] getFilteredCards() {
		this.deck.update();
		Object[] cards = this.deck.getElements();
		List<Object> collection = Arrays.asList(cards);
		return collection.toArray(new MagicCard[collection.size()]);
	}

	@Test
	public void testFilterByColor() {
		add3cards();
		HashMap map = new HashMap();
		String id = Colors.getInstance().getPrefConstant(Colors.getColorName(BLACK_COST));
		map.put(id, "true");
		this.filter.update(map);
		Object[] cards = getFilteredCards();
		assertEquals(1, cards.length);
		assertEquals(BLACK_COST, ((IMagicCard) cards[0]).getCost());
	}

	@Test
	public void testFilterByColor2() {
		add3cards();
		HashMap map = new HashMap();
		map.put(Colors.getInstance().getPrefConstant(Colors.getColorName(BLACK_COST)), "true");
		map.put(Colors.getInstance().getPrefConstant(Colors.getColorName(WHITE_COST)), "true");
		this.filter.update(map);
		Object[] cards = getFilteredCards();
		assertEquals(2, cards.length);
		assertEquals(BLACK_COST, ((IMagicCard) cards[0]).getCost());
		assertEquals(WHITE_COST, ((IMagicCard) cards[1]).getCost());
	}

	@Test
	public void testFilterByColorAndType() {
		add3cards();
		HashMap map = new HashMap();
		map.put(Colors.getInstance().getPrefConstant(Colors.getColorName(RED_COST)), "true");
		map.put(CardTypes.getInstance().getPrefConstant("Creature"), "true");
		this.filter.update(map);
		Object[] cards = getFilteredCards();
		assertEquals(1, cards.length);
		assertEquals(RED_COST, ((IMagicCard) cards[0]).getCost());
	}

	@Test
	public void testFilterByTypeText() {
		add3cards();
		HashMap map = new HashMap();
		String typeId = FilterField.TYPE_LINE.getPrefConstant();
		map.put(typeId, "Elf");
		this.filter.update(map);
		Object[] cards = getFilteredCards();
		assertEquals(1, cards.length);
		assertEquals(BLACK_COST, ((IMagicCard) cards[0]).getCost());
	}

	public void testSortByColor() {
		add3cards();
		this.filter.setSortField(MagicCardField.COST, true);
		Object[] cards = getFilteredCards();
		assertEquals(3, cards.length);
		assertEquals(WHITE_COST, ((MagicCard) cards[0]).getCost());
		assertEquals(BLACK_COST, ((MagicCard) cards[1]).getCost());
		assertEquals(RED_COST, ((MagicCard) cards[2]).getCost());
	}

	public void testSortByType() {
		add3cards();
		this.filter.setSortField(MagicCardField.TYPE, true);
		Object[] cards = getFilteredCards();
		assertEquals(3, cards.length);
		assertEquals(card1, cards[0]);
		assertEquals(card2, cards[1]);
		assertEquals(card3, cards[2]);
	}

	public void testSortAndGroup() {
		MagicCard a0 = addCard(RED_COST, INSTANT, "b1");
		MagicCard a1 = addCard(BLACK_COST, INSTANT, "a1");
		MagicCard a2 = addCard(WHITE_COST, INSTANT, "a2");
		MagicCard a3 = addCard(RED_COST, INSTANT, "a3");
		MagicCard a4 = addCard(ONE_COST, ARTIFACT, "b2");
		this.filter.setGroupField(MagicCardField.COST);
		this.filter.setSortField(MagicCardField.NAME, true);
		MagicCard[] cards = getFilteredCards();
		assertEquals(5, cards.length);
		assertEquals(a1, cards[0]);
		assertEquals(a2, cards[1]);
		assertEquals(a3, cards[2]);
		assertEquals(a0, cards[3]);
		assertEquals(a4, cards[4]);
	}

	public void testSortBy2() {
		MagicCard a0 = addCard(RED_COST, INSTANT, "b");
		MagicCard a1 = addCard(BLACK_COST, INSTANT, "a");
		MagicCard a2 = addCard(WHITE_COST, INSTANT, "a");
		MagicCard a3 = addCard(RED_COST, INSTANT, "a3");
		MagicCard a4 = addCard(ONE_COST, ARTIFACT, "b");
		this.filter.setSortField(MagicCardField.COST, false);
		this.filter.setSortField(MagicCardField.NAME, true);
		this.filter.setSortField(MagicCardField.TYPE, true);
		MagicCard[] cards = getFilteredCards();
		assertEquals(5, cards.length);
		assertEquals(a4, cards[0]);
		assertEquals(a1, cards[1]);
		assertEquals(a2, cards[2]);
		assertEquals(a3, cards[3]);
		assertEquals(a0, cards[4]);
	}
}
