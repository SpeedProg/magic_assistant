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

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.CardGroup;
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

	@Override
	protected void setUp() throws Exception {
		this.deck = new MemCardHandler();
		this.filter = new MagicCardFilter();
	}

	/**
	 * Test method for {@link com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore#update(com.reflexit.magiccards.core.model.MagicCardFilter)}.
	 */
	@Test
	public void testGetCardGrpups() {
		MagicCard card1 = CardGenerator.generateRandomCard();
		card1.setCost("{B}");
		MagicCard card2 = CardGenerator.generateRandomCard();
		card2.setCost("{R}");
		MagicCard card3 = CardGenerator.generateRandomCard();
		card3.setCost("{W}");
		this.deck.getCardStore().add(card1);
		this.deck.getCardStore().add(card2);
		this.deck.getCardStore().add(card3);
		this.filter.setSortField(MagicCardField.COST);
		this.filter.setGroupField(MagicCardField.COST);
		this.deck.update(this.filter);
		Object[] cardGroups = this.deck.getCardGroups();
		assertEquals(3, cardGroups.length);
		assertEquals("Black", ((CardGroup) cardGroups[0]).getName());
		assertEquals("Red", ((CardGroup) cardGroups[1]).getName());
		assertEquals("White", ((CardGroup) cardGroups[2]).getName());
	}
}
