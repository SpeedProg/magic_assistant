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
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;
import com.reflexit.magiccards.core.model.storage.AbstractMultiStore;
import com.reflexit.magiccards.core.model.storage.CollectionCardStore;
import com.reflexit.magiccards.core.model.storage.ICardCollection;

/**
 * @author Alena
 *
 */
public class VirtualMultiFileCardStore extends AbstractMultiStore<IMagicCard> implements ICardCollection<IMagicCard> {
	public VirtualMultiFileCardStore() {
		super();
	}

	public synchronized CollectionCardStore addFile(final File file, final Location location, boolean initialize) {
		if (location != null && map.containsKey(location)) {
			return (CollectionCardStore) map.get(location);
		}
		CollectionCardStore store = new DbFileCardStore(file, location, initialize);
		addCardStore(store);
		if (initialize) {
			initialized = initialize;
			store.initialize();
		}
		return store;
	}

	public int getCount() {
		return getStorage().size();
	}

	@Override
	protected AbstractCardStoreWithStorage newStorage(IMagicCard card) {
		DbFileCardStore store = new DbFileCardStore(getFile(card), getLocation(card), false);
		store.getStorage().setAutoCommit(getStorage().isAutoCommit());
		return store;
	}

	@Override
	protected synchronized boolean doAddAll(Collection<? extends IMagicCard> col) {
		boolean modified = super.doAddAll(col);
		pruneDuplicates();
		return modified;
	}

	/**
	 *
	 */
	public void pruneDuplicates() {
		HashSet<Integer> hash = new HashSet<Integer>();
		ArrayList<IMagicCard> duplicates = new ArrayList<IMagicCard>();
		for (Object element : this) {
			IMagicCard card = (IMagicCard) element;
			if (hash.contains(card.getCardId())) {
				duplicates.add(card);
				continue;
			}
			hash.add(card.getCardId());
			if (Editions.getInstance().getAbbrByName(card.getSet()) == null) {
				System.err.println("Failed to find set: " + card.getSet());
			}
		}
		this.removeAll(duplicates);
	}

	@Override
	protected Location getLocation(IMagicCard card) {
		String set = ((MagicCard) card).getSet();
		Location loc = new Location(getExtFileName(set));
		return loc;
	}

	@Override
	public Location getLocation() {
		return null;
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

	private String getExtFileName(String location) {
		return location.replaceAll("[\\W]", "_");
	}

	public File getFile(final IMagicCard card) {
		try {
			if (card instanceof MagicCard) {
				String key = card.getSet();
				return new File(XmlCardHolder.getDbFolder(), getExtFileName(key) + ".xml");
			} else
				throw new MagicException("Unknown card type");
		} catch (CoreException e) {
			throw new MagicException("Can't resolve file: ", e);
		}
	}
}
