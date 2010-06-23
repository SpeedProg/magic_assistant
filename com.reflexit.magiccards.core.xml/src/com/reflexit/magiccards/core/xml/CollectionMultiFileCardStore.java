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

import org.eclipse.core.runtime.CoreException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;
import com.reflexit.magiccards.core.model.storage.AbstractMultiStore;
import com.reflexit.magiccards.core.model.storage.CollectionCardStore;
import com.reflexit.magiccards.core.model.storage.ICardCollection;

/**
 * @author Alena
 * 
 */
public class CollectionMultiFileCardStore extends AbstractMultiStore<IMagicCard> implements ICardCollection<IMagicCard> {
	public CollectionMultiFileCardStore() {
		super();
	}

	/**
	 * @param file
	 * @param location
	 */
	public CollectionCardStore addFile(final File file, final String location) {
		return addFile(file, location, true);
	}

	public synchronized CollectionCardStore addFile(final File file, final String location, boolean initialize) {
		if (location != null && map.containsKey(location)) {
			return (CollectionCardStore) map.get(location);
		}
		CollectionCardStore store = CollectionSingleFileCardStore.create(file, location, initialize);
		addCardStore(store);
		return store;
	}

	@Override
	protected AbstractCardStoreWithStorage newStorage(IMagicCard card) {
		CollectionSingleFileCardStore store = new CollectionSingleFileCardStore(getFile(card), getLocation(card), false);
		store.getStorage().setAutoCommit(getStorage().isAutoCommit());
		return store;
	}

	public File getFile(final IMagicCard card) {
		try {
			if (card instanceof MagicCard) {
				String key = card.getSet();
				return new File(XmlCardHolder.getDbFolder(), getExtFileName(key) + ".xml");
			} else if (card instanceof MagicCardPhisical) {
				String key = getLocation(card);
				AbstractCardStoreWithStorage<IMagicCard> subTable = this.map.get(key);
				if (subTable == null)
					throw new MagicException("Invalid Key: " + key);
				return ((SingleFileCardStorage) subTable.getStorage()).getFile();
			} else
				throw new MagicException("Unknown card type");
		} catch (CoreException e) {
			throw new MagicException("Can't resolve file: ", e);
		}
	}

	public int getCount() {
		int count = 0;
		for (AbstractCardStoreWithStorage table : map.values()) {
			if (table instanceof ICardCountable) {
				count += ((ICardCountable) table).getCount();
			}
		}
		return count;
	}

	public void clear() {
		for (AbstractCardStoreWithStorage table : map.values()) {
			//table.clear();
		}
	}

	@Override
	protected String getLocation(IMagicCard card) {
		String loc = null;
		if (card instanceof MagicCardPhisical) {
			loc = ((MagicCardPhisical) card).getLocation();
		} else if (card instanceof MagicCard) {
			loc = card.getSet();
		}
		if (loc != null)
			return loc;
		return getLocation();
	}

	public IMagicCard getCard(int id) {
		Collection<IMagicCard> cards = getCards(id);
		if (cards.size() > 0) {
			return cards.iterator().next();
		}
		return null;
	}

	public Collection<IMagicCard> getCards(int id) {
		ArrayList<IMagicCard> arr = new ArrayList<IMagicCard>();
		for (AbstractCardStoreWithStorage table : map.values()) {
			if (table instanceof ICardCollection) {
				IMagicCard card = ((ICardCollection<IMagicCard>) table).getCard(id);
				if (card != null) {
					arr.add(card);
				}
			}
		}
		return arr;
	}

	public File getFile(String location) {
		AbstractCardStoreWithStorage storage = getStorage(location);
		if (storage == null)
			return null;
		File file = ((SingleFileCardStorage) storage.getStorage()).getFile();
		return file;
	}

	private String getExtFileName(String location) {
		return location.replaceAll("[\\W]", "_");
	}

	public String getComment() {
		throw new UnsupportedOperationException();
	}

	public String getName() {
		throw new UnsupportedOperationException();
	}

	public boolean isVirtual() {
		throw new UnsupportedOperationException();
	}
}
