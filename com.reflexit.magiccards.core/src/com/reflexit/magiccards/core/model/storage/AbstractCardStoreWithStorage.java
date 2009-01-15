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
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;

/**
 * @author Alena
 * 
 */
public class AbstractCardStoreWithStorage<T> extends AbstractCardStore<T> implements ICardStore<T>,
        IStorageContainer<T> {
	protected IStorage<T> storage;
	protected boolean wrapped;
	protected boolean processStorageEvents = true;
	private ICardEventListener lis = new ICardEventListener() {
		// event in underline storage should case to reinitialize
		public void handleEvent(CardEvent event) {
			if (processStorageEvents)
				doInitialize();
		}
	};

	/**
	 * 
	 */
	public AbstractCardStoreWithStorage(final IStorage<T> storage, boolean wrapped) {
		super();
		this.storage = storage;
		this.wrapped = wrapped;
		this.storage.addListener(lis);
	}

	public IStorage<T> getStorage() {
		return storage;
	}

	protected boolean doAddAll(Collection<? extends T> list) {
		if (wrapped)
			return super.doAddAll(list);
		else
			return storage.addAll(list);
	}

	public Iterator<T> iterator() {
		return storage.iterator();
	}

	public boolean doRemoveAll() {
		if (wrapped)
			return super.doRemoveAll();
		else
			return storage.removeAll();
	}

	public boolean doRemoveAll(Collection<?> list) {
		if (wrapped)
			return super.doRemoveAll(list);
		else
			return storage.removeAll(list);
	}

	public int size() {
		return storage.size();
	}

	@Override
	protected boolean doAddCard(T card) {
		return storage.add(card);
	}

	@Override
	protected void doInitialize() throws MagicException {
		storage.load();
	}

	@Override
	protected boolean doRemoveCard(T card) {
		return storage.remove(card);
	}

	public void addListener(ICardEventListener lis) {
		storage.addListener(lis);
	}

	public void removeListener(ICardEventListener lis) {
		storage.removeListener(lis);
	}

	public void update(T card) {
		storage.update(card);
	}
}