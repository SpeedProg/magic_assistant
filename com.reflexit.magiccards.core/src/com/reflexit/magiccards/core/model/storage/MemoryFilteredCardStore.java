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

import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICard;

/**
 * @author Alena
 *
 */
public class MemoryFilteredCardStore<T extends ICard> extends AbstractFilteredCardStore<T> {
	public MemoryFilteredCardStore(Collection<T> list) {
		super(new MemoryCardStore<T>());
		addAll(list);
		update();
	}

	public MemoryFilteredCardStore() {
		super(new MemoryCardStore<T>());
		update();
	}

	public void addAll(Collection<T> list) {
		store.addAll(list);
	}

	public void add(T c) {
		store.add(c);
	}

	@Override
	public void setLocation(Location location) {
		store.setLocation(location);
	}

	@Override
	public Location getLocation() {
		return store.getLocation();
	}

	@Override
	public void clear() {
		store.removeAll();
	}

	@Override
	public boolean contains(T card) {
		return store.contains(card);
	}

	public void setFilter(MagicCardFilter filter) {
		this.filter = filter;
	}

	public void update(MagicCardFilter filter) {
		this.filter = filter;
		update();
	}
}
