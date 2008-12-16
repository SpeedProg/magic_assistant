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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.CollectionCardStore;
import com.reflexit.magiccards.core.test.assist.CardGenerator;
import com.reflexit.magiccards.core.xml.DeckFileCardStore;

/**
 * @author Alena
 * 
 */
public class DeckStoreTest extends TestCase {
	CollectionCardStore store;
	protected MagicCard m1;
	protected MagicCard m2;

	@Override
	protected void setUp() throws Exception {
		String name = "aaa";
		File tempFile = File.createTempFile("deck", ".xml");
		tempFile.deleteOnExit();
		this.store = new DeckFileCardStore(tempFile, name, "Decks/" + name + ".xml");
		this.m1 = CardGenerator.generateRandomCard();
		this.m1.setName("name 1");
		this.m2 = CardGenerator.generateRandomCard();
		this.m2.setName("name 2");
	}

	public void testName() {
		assertEquals("aaa", ((DeckFileCardStore) this.store).getDeckName());
	}

	public void testAddCard() {
		MagicCard a = CardGenerator.generateRandomCard();
		this.store.addCard(a);
		assertEquals(this.store.getTotal(), 1);
		for (Iterator iterator = this.store.cardsIterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			assertEquals(a.getCardId(), card.getCardId());
		}
	}

	public void testAddAll() {
		MagicCard a = CardGenerator.generateRandomCard();
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>();
		list.add(a);
		this.store.addAll(list);
		assertEquals(1, this.store.getTotal());
		for (Iterator iterator = this.store.cardsIterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			assertEquals(a.getCardId(), card.getCardId());
		}
	}

	public void testAddCardMCP() {
		MagicCardPhisical a = new MagicCardPhisical(this.m1);
		this.store.addCard(a);
		assertEquals(this.store.getTotal(), 1);
		for (Iterator iterator = this.store.cardsIterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			assertEquals(a.getCardId(), card.getCardId());
		}
	}

	public void testAddCardMCP_2() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		this.store.addCard(a1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m2);
		assertEquals(this.store.getTotal(), 1);
		this.store.addCard(a2);
		assertEquals(this.store.getTotal(), 2);
	}

	public void testAddCardMCP_2Merge() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		this.store.addCard(a1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m1);
		assertEquals(1, this.store.getTotal());
		this.store.addCard(a2);
		assertEquals(1, this.store.getTotal());
		MagicCardPhisical a3 = (MagicCardPhisical) this.store.getCard(this.m1.getCardId());
		assertEquals(2, a3.getCount());
	}

	public void testRemoveCard() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		this.store.addCard(a1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m2);
		assertEquals(1, this.store.getTotal());
		this.store.addCard(a2);
		assertEquals(2, this.store.getTotal());
		this.store.removeCard(a2);
		assertEquals(1, this.store.getTotal());
	}

	public void testRemoveCard2() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		this.store.addCard(a1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m2);
		assertEquals(1, this.store.getTotal());
		this.store.addCard(a2);
		assertEquals(2, this.store.getTotal());
		MagicCardPhisical a3 = (MagicCardPhisical) this.store.getCard(this.m2.getCardId());
		this.store.removeCard(a3);
		assertEquals(1, this.store.getTotal());
	}

	public void testSelfDragAndDrop() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		a1.setCount(1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m2);
		a2.setCount(3);
		this.store.addCard(a1);
		assertEquals(1, this.store.getTotal());
		this.store.addCard(a2);
		assertEquals(2, this.store.getTotal());
		assertEquals(4, this.store.getCount());
		MagicCardPhisical a3 = (MagicCardPhisical) this.store.getCard(this.m2.getCardId());
		this.store.addCard(a3);
		assertEquals(2, this.store.getTotal());
		assertEquals(7, this.store.getCount());
		this.store.removeCard(a3);
		assertEquals(2, this.store.getTotal());
		assertEquals(4, this.store.getCount());
	}

	public void testSelfDragAndDropMerge() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		a1.setCount(1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m1);
		a2.setCount(3);
		this.store.addCard(a1);
		assertEquals(1, this.store.getTotal());
		this.store.setMergeOnAdd(false);
		this.store.addCard(a2);
		this.store.setMergeOnAdd(true);
		assertEquals(2, this.store.getTotal());
		assertEquals(4, this.store.getCount());
		this.store.removeCard(a2);
		assertEquals(1, this.store.getCount());
		this.store.addCard(a2);
		assertEquals(1, this.store.getTotal());
		assertEquals(4, this.store.getCount());
	}

	public void testSelfDragAndDropMerge2() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		a1.setCount(1);
		this.store.addCard(a1);
		assertEquals(1, this.store.getTotal());
		this.store.addCard(a1);
		assertEquals(1, this.store.getTotal());
		assertEquals(2, this.store.getCount());
		this.store.removeCard(a1);
		assertEquals(1, this.store.getCount());
		assertEquals(1, this.store.getTotal());
	}

	public void testAddMint() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		a1.setCount(1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m1);
		a2.setCount(1);
		a2.setCondition("mint");
		this.store.addCard(a1);
		assertEquals(1, this.store.getTotal());
		this.store.addCard(a2);
		assertEquals(2, this.store.getTotal());
		assertEquals(2, this.store.getCount());
	}
}
