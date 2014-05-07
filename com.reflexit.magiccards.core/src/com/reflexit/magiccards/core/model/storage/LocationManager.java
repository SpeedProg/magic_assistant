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

import java.util.HashMap;

import com.reflexit.magiccards.core.model.Location;

/**
 * Keeps the map between locations and card stores. It is not used now.
 */
public class LocationManager<T> {
	private HashMap<Location, ICardStore<T>> map = new HashMap<Location, ICardStore<T>>();
	private static LocationManager instance;

	public static LocationManager getInstance() {
		if (instance == null)
			instance = new LocationManager();
		return instance;
	}

	private LocationManager() {
	}

	public ICardStore<T> addCardStore(Location loc, ICardStore<T> store) {
		return map.put(loc, store);
	}

	public ICardStore<T> getCardStore(Location loc) {
		return map.get(loc);
	}

	public int size() {
		return map.size();
	}

	public boolean containsKey(Location key) {
		return map.containsKey(key);
	}

	public void clear() {
		map.clear();
	}
}
