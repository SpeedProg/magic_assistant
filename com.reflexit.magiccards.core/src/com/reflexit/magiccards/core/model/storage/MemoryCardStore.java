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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.MagicException;

/**
 * ArrayList based implementation for AbstractCardStore
 * @author Alena
 *
 */
public class MemoryCardStore<T> extends AbstractCardStore<T> {
	protected ArrayList<T> list;

	/**
	 * creates empty card store
	 */
	public MemoryCardStore() {
		super();
	}

	@Override
	protected void doInitialize() throws MagicException {
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
		if (this.list == null)
			doInitialize();
		return this.list;
	}

	public void setList(ArrayList<T> list) {
		this.list = list;
	}
}