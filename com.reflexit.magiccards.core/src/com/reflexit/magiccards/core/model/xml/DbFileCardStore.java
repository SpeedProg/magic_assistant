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
package com.reflexit.magiccards.core.model.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardModifiable;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;
import com.reflexit.magiccards.core.model.xml.DbMultiFileCardStore.GlobalDbHandler;

/**
 * Single File store with card count and caching
 *
 * @author Alena
 *
 */
public class DbFileCardStore extends AbstractCardStoreWithStorage<IMagicCard> {
	private GlobalDbHandler handler;

	/**
	 * @param file
	 */
	public DbFileCardStore(File file, Location location, GlobalDbHandler handler) {
		this(file, location, handler, false);
	}

	public DbFileCardStore(File file, Location location, GlobalDbHandler handler, boolean initialize) {
		super(new SingleFileCardStorage(file, location, initialize), false);
		this.handler = handler;
		if (initialize) {
			initialize();
		}
	}

	@Override
	public IMagicCard doAddCard(IMagicCard card) {
		// this would be called when new cards are added or loading from resources
		return storeCard(card);
	}

	@Override
	protected void doInitialize() throws MagicException {
		super.doInitialize();
		setAutoCommit(false);
		try {
			for (Iterator iterator = ((SingleFileCardStorage) storage).getList().iterator(); iterator
					.hasNext();) {
				IMagicCard card = (IMagicCard) iterator.next();
				if (handler.hashAndResolve(card)) {
					iterator.remove();
				}
			}
		} finally {
			setAutoCommit(true);
		}
	}

	@Override
	public boolean doRemoveCard(IMagicCard card) {
		handler.remove(card);
		return getStorage().remove(card);
	}

	@Override
	public IMagicCard getCard(int id) {
		return handler.get(id);
	}

	@Override
	public Collection<IMagicCard> getCards(int id) {
		// System.err.println("getCards called");
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
	private IMagicCard storeCard(IMagicCard card) {
		int id = card.getCardId();
		if (id == 0) {
			// create syntetic id
			// Local db bitset
			// [t2][s15][l4][v1][i10]
			id = ((MagicCard) card).syntesizeId();
			((ICardModifiable) card).set(MagicCardField.ID, id);
		}
		if (handler.hashAndResolve(card) == false) {
			storage.add(card);
		} else {
			storage.autoSave();
		}
		return card;
	}
}
