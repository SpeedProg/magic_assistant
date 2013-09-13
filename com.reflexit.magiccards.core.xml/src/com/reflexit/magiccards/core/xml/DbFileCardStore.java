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
		if (initialize) {
			initialize();
		}
	}

	@Override
	public boolean doAddCard(IMagicCard card) {
		// this would be called when new cards are added or loading from resources
		storeCard(card);
		return true;
	}

	@Override
	protected void doInitialize() throws MagicException {
		super.doInitialize();
		setAutoCommit(false);
		try {
			for (Iterator iterator = ((SingleFileCardStorage) storage).getList().iterator(); iterator.hasNext();) {
				IMagicCard card = (IMagicCard) iterator.next();
				if (hashAndResolve(card)) {
					iterator.remove();
				}
			}
		} finally {
			setAutoCommit(true);
		}
	}

	private boolean hashAndResolve(IMagicCard card) {
		int id = card.getCardId();
		IMagicCard prev = (IMagicCard) this.hash.get(id);
		if (prev != null) {
			boolean delcur = conflictMerge(prev, card);
			hash.put(prev.getCardId(), prev); // rehash prev it could have changed
			if (delcur) {
				return true;
			} else {
				hash.put(card.getCardId(), card); // id could have changed
			}
		} else
			hash.put(id, card);
		return false;
	}

	private boolean conflictMerge(IMagicCard prev, IMagicCard card) {
		if (prev.equals(card)) {
			// merge
			((MagicCard) prev).copyFrom(card);
			return true;
		}
		int id = card.getCardId();
		// redo
		Integer old = (Integer) prev.getObjectByField(MagicCardField.SIDE);
		Integer cur = (Integer) card.getObjectByField(MagicCardField.SIDE);
		Object prevPart = prev.getObjectByField(MagicCardField.PART);
		Object curPart = card.getObjectByField(MagicCardField.PART);
		if (old == 0 && cur == 0) {
			if (prevPart != null)
				old = 1;
			if (curPart != null)
				cur = 1;
		}
		if (old == cur) {
			System.err.println("STORE DOUBLE: " + prev + " " + old + "[" + prevPart + "] -> new " + card + "[" + curPart + "] " + cur);
			return true;
		} else {
			if (old == 1) {
				((ICardModifiable) prev).setObjectByField(MagicCardField.ID, String.valueOf(-id));
				return false;
			} else if (cur == 1) {
				((ICardModifiable) card).setObjectByField(MagicCardField.ID, String.valueOf(-id));
				return false;
			}
		}
		return false;
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
	 * @param b
	 */
	private void storeCard(IMagicCard card) {
		int id = card.getCardId();
		if (id == 0) {
			// create syntetic id
			// Local db bitset
			// [t2][s15][l4][v1][i10]
			id = ((MagicCard) card).syntesizeId();
			((ICardModifiable) card).setObjectByField(MagicCardField.ID, String.valueOf(id));
		}
		if (hashAndResolve(card) == false) {
			storage.add(card);
		} else {
			storage.autoSave();
		}
	}
}
