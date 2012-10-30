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

import com.reflexit.magiccards.core.model.ICardCountable;

/**
 * ArrayList based implementation for AbstractCardStore
 * 
 * @author Alena
 * 
 */
public class MemoryCardStore<T> extends AbstractCardStoreWithStorage<T> implements ICardStore<T>, ICardCountable {
	protected boolean mergeOnAdd = true;

	/**
	 * creates empty card store
	 */
	public MemoryCardStore() {
		super(new MemoryCardStorage<T>(), false);
	}

	@Override
	public void setMergeOnAdd(final boolean v) {
		this.mergeOnAdd = v;
	}

	@Override
	public boolean getMergeOnAdd() {
		return this.mergeOnAdd;
	}

	public T getCard(int id) {
		return null;
	}

	public Collection<T> getCards(int id) {
		throw new UnsupportedOperationException();
	}

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
}