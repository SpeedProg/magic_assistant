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
package com.reflexit.magiccards.core.model.storage;

import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.abs.ICard;

/**
 * @author Alena
 *
 */
public abstract class AbstractCardStoreWithStorage<T extends ICard> extends AbstractCardStore<T> implements
		ICardStore<T>,
		IStorageContainer<T> {
	protected IStorage<T> storage;
	protected boolean wrapped;

	/**
	 *
	 */
	public AbstractCardStoreWithStorage(final IStorage<T> storage, boolean wrapped) {
		super();
		this.storage = storage;
		this.wrapped = wrapped;
	}

	@Override
	public IStorage<T> getStorage() {
		return storage;
	}

	@Override
	protected synchronized boolean doAddAll(Collection<? extends T> list) {
		if (wrapped)
			return super.doAddAll(list);
		else
			return storage.addAll(list);
	}

	@Override
	public Iterator<T> iterator() {
		return storage.iterator();
	}

	@Override
	public boolean doRemoveAll() {
		if (wrapped)
			return super.doRemoveAll();
		else
			return storage.removeAll();
	}

	@Override
	public boolean doRemoveAll(Collection<? extends T> list) {
		if (wrapped)
			return super.doRemoveAll(list);
		else
			return storage.removeAll(list);
	}

	@Override
	public int size() {
		return storage.size();
	}

	@Override
	protected T doAddCard(T card) {
		if (storage.add(card)) return card;
		return null;
	}

	@Override
	protected void doInitialize() throws MagicException {
		storage.load();
	}

	@Override
	protected boolean doRemoveCard(T card) {
		return storage.remove(card);
	}

	@Override
	public Location getLocation() {
		return storage.getLocation();
	}

	@Override
	public void setLocation(Location location) {
		storage.setLocation(location);
	}

	@Override
	public String getComment() {
		return storage.getComment();
	}

	@Override
	public String getName() {
		return storage.getName();
	}

	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	public void setVirtual(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isVirtual() {
		return storage.isVirtual();
	}

	@Override
	public boolean contains(T card) {
		return storage.contains(card);
	}
}