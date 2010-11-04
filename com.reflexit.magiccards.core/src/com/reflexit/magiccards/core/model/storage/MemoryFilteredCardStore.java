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

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardFilter;

/**
 * @author Alena
 * 
 */
public class MemoryFilteredCardStore extends AbstractFilteredCardStore<IMagicCard> {
	private MemoryCardStore cards = new MemoryCardStore();

	public MemoryFilteredCardStore(Collection<IMagicCard> list) {
		this();
		addAll(list);
	}

	public MemoryFilteredCardStore() {
		update(new MagicCardFilter());
	}

	public void addAll(Collection<IMagicCard> list) {
		cards.addAll(list);
	}

	public ICardStore getCardStore() {
		return this.cards;
	}

	@Override
	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location getLocation() {
		throw new UnsupportedOperationException();
	}
}
