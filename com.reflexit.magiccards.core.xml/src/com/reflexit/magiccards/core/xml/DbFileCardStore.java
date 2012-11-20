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
package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.ICardModifiable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;
import com.reflexit.magiccards.core.model.storage.ICardSet;
import com.reflexit.magiccards.core.model.storage.MemoryCardStorage;
import com.reflexit.magiccards.core.model.utils.IntHashtable;

/**
 * Single File store with card count and caching
 * 
 * @author Alena
 * 
 */
public class DbFileCardStore extends AbstractCardStoreWithStorage<IMagicCard> {
	IntHashtable hash;

	/**
	 * @param file
	 */
	public DbFileCardStore(File file, Location location, IntHashtable hash) {
		this(file, location, hash, false);
	}

	public DbFileCardStore(File file, Location location, IntHashtable hash, boolean initialize) {
		super(new SingleFileCardStorage(file, location, initialize), false);
		this.hash = hash;
	}

	@Override
	public boolean doAddCard(IMagicCard card) {
		// db does not actually add cards but rather updates
		storeCard(card, storage, storage);
		return true;
	}

	@Override
	protected void doInitialize() throws MagicException {
		super.doInitialize();
		MemoryCardStorage<IMagicCard> toRemove = new MemoryCardStorage<IMagicCard>() {
			@Override
			public boolean remove(IMagicCard card) {
				return super.add(card); // yes add
			}
		};
		MemoryCardStorage<IMagicCard> toAdd = new MemoryCardStorage<IMagicCard>();
		for (Iterator iterator = storage.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			storeCard(card, toRemove, toAdd);
		}
		if (toRemove.size() > 0 || toAdd.size() > 0) {
			storage.removeAll(toRemove.getList());
			storage.addAll(toAdd.getList());
			storage.save();
		}
	}

	@Override
	public boolean doRemoveCard(IMagicCard card) {
		hash.remove(card.getCardId());
		return getStorage().remove(card);
	}

	@Override
	public IMagicCard getCard(int id) {
		return (IMagicCard) hash.get(id);
	}

	@Override
	public Collection<IMagicCard> getCards(int id) {
		System.err.println("getCards called");
		IMagicCard card = getCard(id);
		if (card == null)
			return Collections.EMPTY_LIST;
		ArrayList<IMagicCard> arr = new ArrayList<IMagicCard>(1);
		arr.add(card);
		return arr;
	}

	/**
	 * @param card
	 * @param toAdd
	 * @param toRemove
	 */
	private void storeCard(IMagicCard card, ICardSet<IMagicCard> toRemove, ICardSet<IMagicCard> toAdd) {
		int id = card.getCardId();
		if (id == 0) {
			// create syntetic id
			// Local db bitset
			// [t2][s15][l4][v1][i10]
			id = 1 << 31 | card.getSet().hashCode() & 0x7f << 15 | card.getName().hashCode() & 0x3f;
			((ICardModifiable) card).setObjectByField(MagicCardField.ID, String.valueOf(id));
		}
		IMagicCard prev = (IMagicCard) this.hash.get(id);
		if (prev == null) {
			// add
			this.hash.put(id, card);
			toAdd.add(card);
			return;
		}
		if (prev instanceof MagicCard && card.equals(prev)) {
			// merge
			toRemove.remove(card);
			((MagicCard) prev).copyFrom(card);
			toAdd.add(prev);
			return;
		}
		// redo
		Integer old = (Integer) prev.getObjectByField(MagicCardField.SIDE);
		Integer cur = (Integer) card.getObjectByField(MagicCardField.SIDE);
		if (old == cur) {
			System.err.println("STORE DOUBLE: " + prev + " " + old + "[" + prev.getObjectByField(MagicCardField.PART) + "] -> new " + card
					+ "[" + card.getObjectByField(MagicCardField.PART) + "] " + cur);
		} else {
			if (old == 1) {
				toRemove.remove(prev);
				hash.remove(id);
				((ICardModifiable) prev).setObjectByField(MagicCardField.ID, String.valueOf(-id));
				hash.put(-id, prev);
				hash.put(id, card);
				toAdd.add(prev);
				toAdd.add(card);
				return;
			} else if (cur == 1) {
				((ICardModifiable) card).setObjectByField(MagicCardField.ID, String.valueOf(-id));
				hash.put(-id, card);
				toRemove.add(card);
				return;
			}
		}
		this.hash.put(id, card);
		toAdd.add(card);
		return;
	}
}
