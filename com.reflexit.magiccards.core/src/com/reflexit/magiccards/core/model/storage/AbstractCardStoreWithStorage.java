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
public abstract class AbstractCardStoreWithStorage<T> extends AbstractCardStore<T> {
	protected IStorage<T> storage;

	/**
	 * 
	 */
	public AbstractCardStoreWithStorage(final IStorage<T> storage) {
		super();
		this.storage = storage;
	}

	public Iterator<T> cardsIterator() {
		return this.storage.cardsIterator();
	}

	public int getTotal() {
		return this.storage.getTotal();
	}

	@Override
	protected synchronized boolean doAddCard(final T card) {
		return this.storage.addCard(card);
	}

	@Override
	protected synchronized boolean doRemoveCard(final T card) {
		return this.storage.removeCard(card);
	}

	@Override
	protected synchronized void doAddAll(final Collection<T> col) {
		this.storage.addAll(col);
	}

	@Override
	protected synchronized void doInitialize() throws MagicException {
		this.storage.initialize();
	}

	protected IStorage<T> getStorage() {
		return storage;
	}
}