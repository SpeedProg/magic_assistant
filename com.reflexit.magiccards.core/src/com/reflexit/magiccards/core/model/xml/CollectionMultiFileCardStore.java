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

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;
import com.reflexit.magiccards.core.model.storage.AbstractMultiStore;
import com.reflexit.magiccards.core.model.storage.CollectionCardStore;
import com.reflexit.magiccards.core.model.storage.ICardCollection;

/**
 * @author Alena
 *
 */
public class CollectionMultiFileCardStore extends AbstractMultiStore<IMagicCard> implements
		ICardCollection<IMagicCard> {
	public CollectionMultiFileCardStore() {
		super();
	}

	/**
	 * @param file
	 * @param location
	 */
	public CollectionCardStore addFile(final File file, final Location location) {
		return addFile(file, location, true);
	}

	public synchronized CollectionCardStore addFile(final File file, final Location location,
			boolean initialize) {
		if (location != null && map.containsKey(location)) {
			return (CollectionCardStore) map.get(location);
		}
		CollectionCardStore store = CollectionSingleFileCardStore.create(file, location, initialize);
		addCardStore(store);
		return store;
	}

	@Override
	protected AbstractCardStoreWithStorage newStorage(IMagicCard card) {
		CollectionSingleFileCardStore store = new CollectionSingleFileCardStore(getFile(card),
				getLocation(card), false);
		store.getStorage().setAutoCommit(getStorage().isAutoCommit());
		return store;
	}

	public File getFile(final IMagicCard card) {
		if (card instanceof MagicCardPhysical) {
			Location key = getLocation(card);
			AbstractCardStoreWithStorage<IMagicCard> subTable = this.map.get(key);
			if (subTable == null)
				throw new MagicException("Invalid Key: " + key);
			return ((SingleFileCardStorage) subTable.getStorage()).getFile();
		} else
			throw new MagicException("Unknown card type:" + card.getClass());
	}

	@Override
	public int getCount() {
		int count = 0;
		for (AbstractCardStoreWithStorage table : map.values()) {
			if (table instanceof ICardCountable) {
				count += ((ICardCountable) table).getCount();
			}
		}
		return count;
	}

	@Override
	protected Location getLocation(IMagicCard card) {
		Location loc = null;
		if (card instanceof MagicCardPhysical) {
			loc = ((MagicCardPhysical) card).getLocation();
		}
		if (loc != null)
			return loc;
		return getLocation();
	}

	public File getFile(Location location) {
		AbstractCardStoreWithStorage storage = getStorage(location);
		if (storage == null)
			return null;
		File file = ((SingleFileCardStorage) storage.getStorage()).getFile();
		return file;
	}

	@Override
	public String getComment() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isVirtual() {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized IMagicCard doAddCard(IMagicCard card) {
		Location loc = getLocation();
		if (card instanceof MagicCardPhysical)
			loc = ((MagicCardPhysical) card).getLocation();
		MagicCardPhysical c = new MagicCardPhysical(card, loc);
		return super.doAddCard(c);
	}
}
