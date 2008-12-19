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

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.CollectionCardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.test.assist.CardGenerator;
import com.reflexit.magiccards.core.xml.DeckFileCardStore;
import com.reflexit.magiccards.core.xml.SingleFileCardStorage;

/**
 * @author Alena
 * 
 */
public class DeckStoreTest extends TestCase {
	CollectionCardStore store;
	protected MagicCard m1;
	protected MagicCard m2;
	protected File tempFile;

	@Override
	protected void setUp() throws Exception {
		String name = "aaa";
		tempFile = File.createTempFile("deck", ".xml");
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
		this.store.add(a);
		assertEquals(this.store.size(), 1);
		for (Object element : this.store) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
		}
	}

	public void testAddAll() {
		MagicCard a = CardGenerator.generateRandomCard();
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>();
		list.add(a);
		this.store.addAll(list);
		assertEquals(1, this.store.size());
		for (Object element : this.store) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
		}
	}

	public void testAddCardMCP() {
		MagicCardPhisical a = new MagicCardPhisical(this.m1);
		this.store.add(a);
		assertEquals(this.store.size(), 1);
		for (Object element : this.store) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
		}
	}

	public void testAddCardMCP_2() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		this.store.add(a1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m2);
		assertEquals(this.store.size(), 1);
		this.store.add(a2);
		assertEquals(this.store.size(), 2);
	}

	public void testAddCardMCP_2Merge() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		this.store.add(a1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m1);
		assertEquals(1, this.store.size());
		this.store.add(a2);
		assertEquals(1, this.store.size());
		MagicCardPhisical a3 = (MagicCardPhisical) this.store.getCard(this.m1.getCardId());
		assertEquals(2, a3.getCount());
	}

	public void testRemoveCard() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		this.store.add(a1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m2);
		assertEquals(1, this.store.size());
		this.store.add(a2);
		assertEquals(2, this.store.size());
		this.store.remove(a2);
		assertEquals(1, this.store.size());
	}

	public void testRemoveCard2() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		this.store.add(a1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m2);
		assertEquals(1, this.store.size());
		this.store.add(a2);
		assertEquals(2, this.store.size());
		MagicCardPhisical a3 = (MagicCardPhisical) this.store.getCard(this.m2.getCardId());
		this.store.remove(a3);
		assertEquals(1, this.store.size());
	}

	public void testSelfDragAndDrop() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		a1.setCount(1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m2);
		a2.setCount(3);
		this.store.add(a1);
		assertEquals(1, this.store.size());
		this.store.add(a2);
		assertEquals(2, this.store.size());
		assertEquals(4, this.store.getCount());
		MagicCardPhisical a3 = (MagicCardPhisical) this.store.getCard(this.m2.getCardId());
		this.store.add(a3);
		assertEquals(2, this.store.size());
		assertEquals(7, this.store.getCount());
		this.store.remove(a3);
		assertEquals(2, this.store.size());
		assertEquals(4, this.store.getCount());
	}

	public void testSelfDragAndDropMerge() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		a1.setCount(1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m1);
		a2.setCount(3);
		this.store.add(a1);
		assertEquals(1, this.store.size());
		this.store.setMergeOnAdd(false);
		this.store.add(a2);
		this.store.setMergeOnAdd(true);
		assertEquals(2, this.store.size());
		assertEquals(4, this.store.getCount());
		this.store.remove(a2);
		assertEquals(1, this.store.getCount());
		this.store.add(a2);
		assertEquals(1, this.store.size());
		assertEquals(4, this.store.getCount());
	}

	public void testSelfDragAndDropMerge2() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		a1.setCount(1);
		this.store.add(a1);
		assertEquals(1, this.store.size());
		this.store.add(a1);
		assertEquals(1, this.store.size());
		assertEquals(2, this.store.getCount());
		this.store.remove(a1);
		assertEquals(1, this.store.getCount());
		assertEquals(1, this.store.size());
	}

	public void testAddMint() {
		MagicCardPhisical a1 = new MagicCardPhisical(this.m1);
		a1.setCount(1);
		MagicCardPhisical a2 = new MagicCardPhisical(this.m1);
		a2.setCount(1);
		a2.setCondition("mint");
		this.store.add(a1);
		assertEquals(1, this.store.size());
		this.store.add(a2);
		assertEquals(2, this.store.size());
		assertEquals(2, this.store.getCount());
	}

	public void testAddCardCheckSaved() {
		MagicCard a = CardGenerator.generateRandomCard();
		this.store.add(a);
		String def = ((ILocatable) this.store).getLocation();
		File tempFile1 = tempFile;
		SingleFileCardStorage loaded = new SingleFileCardStorage(tempFile1, def, true);
		assertEquals(1, loaded.size());
		boolean found = false;
		for (Object element : loaded) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
			assertEquals(def, ((MagicCardPhisical) card).getLocation());
			found = true;
		}
		assertTrue("Card not found", found);
	}
}
