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

/**
 * @author Alena
 * 
 */
public abstract class AbstractCardStoreWithStorage<T> extends AbstractCardStore<T> implements IStorageContainer<T> {
	protected IStorage<T> storage;

	/**
	 * 
	 */
	public AbstractCardStoreWithStorage(final IStorage<T> storage) {
		super();
		this.storage = storage;
	}

	public Iterator<T> iterator() {
		return this.storage.iterator();
	}

	public int size() {
		return this.storage.size();
	}

	@Override
	protected synchronized boolean doAddCard(final T card) {
		return this.storage.add(card);
	}

	@Override
	protected synchronized boolean doRemoveCard(final T card) {
		return this.storage.remove(card);
	}

	@Override
	protected synchronized boolean doAddAll(Collection<? extends T> col) {
		return this.storage.addAll(col);
	}

	@Override
	protected boolean doRemoveAll(Collection<?> list) {
		return this.storage.removeAll(list);
	}

	@Override
	protected boolean doUpdate(T card) {
		if (storage.isAutoCommit())
			storage.save();
		return true;
	}

	@Override
	protected boolean doRemoveAll() {
		return this.storage.removeAll();
	}

	@Override
	protected synchronized void doInitialize() throws MagicException {
		this.storage.load();
	}

	public IStorage<T> getStorage() {
		return storage;
	}
}