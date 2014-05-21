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
import java.util.List;

import com.reflexit.magiccards.core.model.ICardCountable;

/**
 * ArrayList based implementation for AbstractCardStore
 * 
 * @author Alena
 * 
 */
public class MemoryCardStore<T> extends AbstractCardStoreWithStorage<T> implements ICardCountable {
	/**
	 * creates empty card store
	 */
	public MemoryCardStore() {
		super(new MemoryCardStorage<T>(), false);
	}

	@Override
	public T getCard(int id) {
		return null;
	}

	public T get(int index) {
		return ((MemoryCardStorage<T>) getStorage()).getList().get(index);
	}

	public List<T> getList() {
		return ((MemoryCardStorage<T>) getStorage()).getList();
	}

	@Override
	public Collection<T> getCards(int id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCount() {
		int count = 0;
		boolean countable = false;
		for (T card : getStorage()) {
			if (card instanceof ICardCountable) {
				count += ((ICardCountable) card).getCount();
				countable = true;
			}
		}
		if (countable)
			return count;
		return size();
	}

	public void clear() {
		getList().clear();
	}
}