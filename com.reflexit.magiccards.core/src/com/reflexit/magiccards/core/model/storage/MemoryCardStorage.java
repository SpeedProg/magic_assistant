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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * ArrayList based implementation for AbstractCardStore
 * @author Alena
 *
 */
public class MemoryCardStorage<T> extends AbstractStorage<T> {
	protected ArrayList<T> list;

	/**
	 * creates empty card store
	 */
	public MemoryCardStorage() {
		super();
		this.list = new ArrayList<T>();
	}

	public Iterator<T> iterator() {
		return this.getList().iterator();
	}

	public int size() {
		return this.getList().size();
	}

	@Override
	public boolean doRemoveCard(T card) {
		return this.getList().remove(card);
	}

	@Override
	public boolean doAddCard(T card) {
		return this.getList().add(card);
	}

	protected boolean doUpdate(T card) {
		return true;
	};

	/**
	 * @return the list
	 */
	public Collection<T> getList() {
		return this.list;
	}

	public void setList(ArrayList<T> list) {
		this.list = list;
	}

	@Override
	public void clearCache() {
		list.clear();
	}

	@Override
	protected void doLoad() {
		// nothing
	}

	@Override
	protected void doSave() throws FileNotFoundException {
		// nothing
	}
}