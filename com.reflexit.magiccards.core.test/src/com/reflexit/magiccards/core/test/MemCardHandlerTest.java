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

import org.junit.Test;

import java.util.HashMap;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.CardTypes;
import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.test.assist.CardGenerator;
import com.reflexit.magiccards.core.test.assist.MemCardHandler;

/**
 * @author Alena
 *
 */
public class MemCardHandlerTest extends TestCase {
	MemCardHandler deck;
	MagicCardFilter filter;
	private MagicCard card1;
	private MagicCard card2;
	private MagicCard card3;

	@Override
	protected void setUp() throws Exception {
		this.deck = new MemCardHandler();
		this.filter = new MagicCardFilter();
	}

	private void add3cards() {
		card1 = CardGenerator.generateRandomCard();
		card1.setCost("{B}");
		card1.setType("Creature - Elf");
		card2 = CardGenerator.generateRandomCard();
		card2.setCost("{R}");
		card2.setType("Creature - Goblin");
		card3 = CardGenerator.generateRandomCard();
		card3.setCost("{W}");
		card3.setType("Instant");
		this.deck.getCardStore().add(card1);
		this.deck.getCardStore().add(card2);
		this.deck.getCardStore().add(card3);
	}

	/**
	 * Test method for {@link com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore#update(com.reflexit.magiccards.core.model.MagicCardFilter)}.
	 */
	@Test
	public void testGetCardGrpups() {
		add3cards();
		this.filter.setSortField(MagicCardField.COST);
		this.filter.setGroupField(MagicCardField.COST);
		this.deck.update(this.filter);
		Object[] cardGroups = this.deck.getCardGroups();
		assertEquals(3, cardGroups.length);
		assertEquals("Black", ((CardGroup) cardGroups[0]).getName());
		assertEquals("Red", ((CardGroup) cardGroups[1]).getName());
		assertEquals("White", ((CardGroup) cardGroups[2]).getName());
	}

	@Test
	public void testFilterByColor() {
		add3cards();
		HashMap map = new HashMap();
		String id = Colors.getInstance().getPrefConstant(Colors.getColorName("{B}"));
		map.put(id, "true");
		this.filter.update(map);
		this.deck.update(this.filter);
		Object[] cards = this.deck.getElements();
		assertEquals(1, cards.length);
		assertEquals("{B}", ((IMagicCard) cards[0]).getCost());
	}

	@Test
	public void testFilterByColor2() {
		add3cards();
		HashMap map = new HashMap();
		map.put(Colors.getInstance().getPrefConstant(Colors.getColorName("{B}")), "true");
		map.put(Colors.getInstance().getPrefConstant(Colors.getColorName("{W}")), "true");
		this.filter.update(map);
		this.deck.update(this.filter);
		Object[] cards = this.deck.getElements();
		assertEquals(2, cards.length);
		assertEquals("{B}", ((IMagicCard) cards[0]).getCost());
		assertEquals("{W}", ((IMagicCard) cards[1]).getCost());
	}

	@Test
	public void testFilterByColorAndType() {
		add3cards();
		HashMap map = new HashMap();
		map.put(Colors.getInstance().getPrefConstant(Colors.getColorName("{R}")), "true");
		map.put(CardTypes.getInstance().getPrefConstant("Creature"), "true");
		this.filter.update(map);
		this.deck.update(this.filter);
		Object[] cards = this.deck.getElements();
		assertEquals(1, cards.length);
		assertEquals("{R}", ((IMagicCard) cards[0]).getCost());
	}

	@Test
	public void testFilterByTypeText() {
		add3cards();
		HashMap map = new HashMap();
		String typeId = FilterHelper.getPrefConstant(FilterHelper.SUBTYPE, FilterHelper.TEXT_POSTFIX);
		map.put(typeId, "Elf");
		this.filter.update(map);
		this.deck.update(this.filter);
		Object[] cards = this.deck.getElements();
		assertEquals(1, cards.length);
		assertEquals("{B}", ((IMagicCard) cards[0]).getCost());
	}
}
