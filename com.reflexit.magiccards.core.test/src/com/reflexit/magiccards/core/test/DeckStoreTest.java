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
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.ICardCollection;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.utils.CardGenerator;
import com.reflexit.magiccards.core.xml.CollectionSingleFileCardStore;

/**
 * @author Alena
 * 
 */
public class DeckStoreTest extends TestCase {
	ICardCollection<IMagicCard> store;
	protected MagicCard m1;
	protected MagicCard m2;
	protected File tempFile;

	@Override
	protected void setUp() throws Exception {
		Location loc = new Location("Decks/aaa.xml");
		tempFile = File.createTempFile("deck", ".xml");
		tempFile.deleteOnExit();
		this.store = new CollectionSingleFileCardStore(tempFile, loc);
		// ((CollectionSingleFileCardStore) this.store).setName(name);
		((CollectionSingleFileCardStore) this.store).setType(IStorageInfo.DECK_TYPE);
		this.m1 = CardGenerator.generateRandomCard();
		this.m1.setName("name 1");
		this.m2 = CardGenerator.generateRandomCard();
		this.m2.setName("name 2");
	}

	@Override
	protected void tearDown() throws Exception {
		tempFile.delete();
		super.tearDown();
	}

	public void testName() {
		assertEquals("aaa", this.store.getName());
	}

	private MagicCard createDCard() {
		MagicCard a = CardGenerator.generateRandomCard();
		a.setSet("Elena");
		return a;
	}

	public void testAddCard() {
		MagicCard a = createDCard();
		this.store.add(a);
		assertEquals(1, this.store.size());
		for (Object element : this.store) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
		}
	}

	public void testAddAll() {
		MagicCard a = createDCard();
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
		MagicCardPhysical a = createMCard(this.m1);
		this.store.add(a);
		assertEquals(this.store.size(), 1);
		for (Object element : this.store) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
		}
	}

	public void testAddCardMCP_2() {
		MagicCardPhysical a1 = createMCard(this.m1);
		this.store.add(a1);
		MagicCardPhysical a2 = createMCard(this.m2);
		assertEquals(this.store.size(), 1);
		this.store.add(a2);
		assertEquals(this.store.size(), 2);
	}

	public void testAddCardMCP_2Merge() {
		MagicCardPhysical a1 = createMCard(this.m1);
		this.store.add(a1);
		MagicCardPhysical a2 = createMCard(this.m1);
		assertEquals(1, this.store.size());
		this.store.add(a2);
		assertEquals(1, this.store.size());
		MagicCardPhysical a3 = (MagicCardPhysical) this.store.getCard(this.m1.getCardId());
		assertEquals(2, a3.getCount());
	}

	public void testRemoveCard() {
		MagicCardPhysical a1 = createMCard(this.m1);
		this.store.add(a1);
		MagicCardPhysical a2 = createMCard(this.m2);
		assertEquals(1, this.store.size());
		this.store.add(a2);
		assertEquals(2, this.store.size());
		this.store.remove(a2);
		assertEquals(1, this.store.size());
	}

	public void testRemoveCard2() {
		MagicCardPhysical a1 = createMCard(this.m1);
		this.store.add(a1);
		MagicCardPhysical a2 = createMCard(this.m2);
		assertEquals(1, this.store.size());
		this.store.add(a2);
		assertEquals(2, this.store.size());
		MagicCardPhysical a3 = (MagicCardPhysical) this.store.getCard(this.m2.getCardId());
		this.store.remove(a3);
		assertEquals(1, this.store.size());
	}

	public void testSelfDragAndDrop() {
		MagicCardPhysical a1 = createMCard(this.m1);
		a1.setCount(1);
		MagicCardPhysical a2 = createMCard(this.m2);
		a2.setCount(3);
		this.store.add(a1);
		assertEquals(1, this.store.size());
		this.store.add(a2);
		assertEquals(2, this.store.size());
		assertEquals(4, this.store.getCount());
		MagicCardPhysical a3 = (MagicCardPhysical) this.store.getCard(this.m2.getCardId());
		this.store.add(a3);
		assertEquals(2, this.store.size());
		assertEquals(7, this.store.getCount());
		this.store.remove(a3);
		assertEquals(2, this.store.size());
		assertEquals(4, this.store.getCount());
	}

	public void testSelfDragAndDropMerge() {
		MagicCardPhysical a1 = createMCard(this.m1);
		a1.setCount(1);
		MagicCardPhysical a2 = createMCard(this.m1);
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
		MagicCardPhysical a1 = createMCard(this.m1);
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

	private MagicCardPhysical createMCard(MagicCard m) {
		return new MagicCardPhysical(m, null);
	}

	public void testAddMint() {
		MagicCardPhysical a1 = createMCard(this.m1);
		a1.setCount(1);
		MagicCardPhysical a2 = createMCard(this.m1);
		a2.setCount(1);
		a2.setCustom("mint");
		this.store.add(a1);
		assertEquals(1, this.store.size());
		this.store.add(a2);
		assertEquals(2, this.store.size());
		assertEquals(2, this.store.getCount());
	}

	public void testAddCardCheckSaved() {
		MagicCard a = createDCard();
		this.store.add(a);
		Location def = ((ILocatable) this.store).getLocation();
		File tempFile1 = tempFile;
		CollectionSingleFileCardStore loaded = new CollectionSingleFileCardStore(tempFile1, def, true);
		assertEquals(1, loaded.size());
		boolean found = false;
		for (Object element : loaded) {
			IMagicCard card = (IMagicCard) element;
			assertEquals(a.getCardId(), card.getCardId());
			assertEquals(def, ((MagicCardPhysical) card).getLocation());
			found = true;
		}
		assertTrue("Card not found", found);
	}
}
