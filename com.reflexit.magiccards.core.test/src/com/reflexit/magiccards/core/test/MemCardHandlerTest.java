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

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
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
		this.deck.getCardStore().addCard(card1);
		this.deck.getCardStore().addCard(card2);
		this.filter.setSortIndex(IMagicCard.INDEX_COST);
		this.filter.setGroupIndex(IMagicCard.INDEX_COST);
		this.deck.update(this.filter);
		Object[] cardGroups = this.deck.getCardGroups();
		assertEquals(2, cardGroups.length);
		assertEquals("Black", ((CardGroup) cardGroups[0]).getName());
		assertEquals("Red", ((CardGroup) cardGroups[1]).getName());
	}
}
