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

/**
 * @author Alena
 * 
 */
public class MemoryFilteredCardStore<T> extends AbstractFilteredCardStore<T> {
	private MemoryCardStore cards = new MemoryCardStore();

	public MemoryFilteredCardStore(Collection<T> list) {
		this();
		addAll(list);
	}

	public MemoryFilteredCardStore() {
		update();
	}

	public void addAll(Collection<T> list) {
		cards.addAll(list);
	}

	public void add(T c) {
		cards.add(c);
	}

	@Override
	public ICardStore getCardStore() {
		return this.cards;
	}

	@Override
	public void setLocation(Location location) {
		cards.setLocation(location);
	}

	@Override
	public Location getLocation() {
		return cards.getLocation();
	}

	@Override
	public void clear() {
		cards.removeAll();
	}

	@Override
	public boolean contains(T card) {
		return cards.contains(card);
	}

	@Override
	public void addAll(ICardStore store) {
		for (Object object : store) {
			cards.add(object);
		}
	}

	public void setFilter(MagicCardFilter filter) {
		this.filter = filter;
	}

	public void update(MagicCardFilter filter) {
		this.filter = filter;
		update();
	}
}
