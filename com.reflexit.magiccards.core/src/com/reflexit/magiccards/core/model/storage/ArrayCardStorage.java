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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.reflexit.magiccards.core.model.Location;


public class ArrayCardStorage<T> extends AbstractStorage<T> {
	private final T array[];
	private final Location location;

	public ArrayCardStorage(T objects[], Location location) {
		super();
		this.array = objects;
		this.location = location;
	}

	public T[] getElements() {
		return array;
	}

	@Override
	public Iterator<T> iterator() {
		return Arrays.asList(array).iterator();
	}

	@Override
	public int size() {
		return array.length;
	}

	@Override
	public boolean doRemoveCard(T card) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean doAddCard(T card) {
		throw new UnsupportedOperationException();
	}

	protected boolean doUpdate(@SuppressWarnings("unused") T card) {
		return true;
	}

	/**
	 * @return the list
	 */
	public List<T> getList() {
		return Arrays.asList(array);
	}


	@Override
	public void clearCache() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void doLoad() {
		// nothing
	}

	/**
	 * @throws IOException
	 */
	@Override
	protected void doSave() throws IOException {
		// nothing
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getComment() {
		return null;
	}

	@Override
	public String getName() {
		return location.getName();
	}

	@Override
	public boolean isVirtual() {
		return true;
	}

	@Override
	public String toString() {
		return getList().toString();
	}
}