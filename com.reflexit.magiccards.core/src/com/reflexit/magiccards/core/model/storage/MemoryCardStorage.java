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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.reflexit.magiccards.core.model.Location;

/**
 * ArrayList based implementation for AbstractCardStore
 * 
 * @author Alena
 * 
 */
public class MemoryCardStorage<T> extends AbstractStorage<T> {
	protected List<T> list;

	/**
	 * creates empty card store
	 */
	public MemoryCardStorage() {
		super();
		this.list = Collections.synchronizedList(new ArrayList<T>());
	}

	public Iterator<T> iterator() {
		synchronized (list) {
			ArrayList x = new ArrayList(list);
			return x.iterator();
		}
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

	protected void doSetList(List<T> list) {
		this.list = Collections.synchronizedList(list);
	}

	@Override
	public void clearCache() {
		list.clear();
		setLoaded(false);
	}

	@Override
	protected void doLoad() {
		// nothing
	}

	@Override
	protected void doSave() throws IOException {
		// nothing
	}

	private final Location MEM_LOC = new Location("mem");

	public Location getLocation() {
		return MEM_LOC;
	}

	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	public String getComment() {
		return null;
	}

	public String getName() {
		return "mem";
	}

	public boolean isVirtual() {
		return true;
	}
}