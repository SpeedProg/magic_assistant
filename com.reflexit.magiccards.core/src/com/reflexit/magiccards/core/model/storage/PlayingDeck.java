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
import java.util.Comparator;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;

/**
 * @author Alena
 * 
 */
public class PlayingDeck extends AbstractFilteredCardStore<IMagicCard> {
	ICardStore deck;
	MemoryCardStore hand;
	MemoryCardStore library;

	static class DrawDeck extends MemoryCardStore {
		@Override
		public int getCount() {
			return size();
		}
	}

	/**
	 * 
	 */
	public PlayingDeck(ICardStore store) {
		setStore(store);
	}

	@Override
	protected Comparator<ICard> getSortComparator(MagicCardFilter filter) {
		Comparator<ICard> comp = filter.getSortOrder().getComparator();
		return comp;
	}

	public void setStore(ICardStore store) {
		if (this.deck != store) {
			this.deck = store;
			shuffle();
			draw(7);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.storage.IFilteredCardStore#getCardStore ()
	 */
	public ICardStore getCardStore() {
		return this.hand;
	}

	public void draw(int cards) {
		int i = 0;
		for (Iterator iterator = this.library.iterator(); iterator.hasNext() && i < cards; i++) {
			IMagicCard card = (IMagicCard) iterator.next();
			this.hand.add(card);
			this.library.remove(card);
		}
	}

	/**
	 * 
	 */
	public void shuffle() {
		Collection<IMagicCard> randomize = CardStoreUtils.randomize(this.deck);
		this.hand = new DrawDeck();
		this.library = new DrawDeck();
		this.library.addAll(randomize);
	}

	@Override
	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location getLocation() {
		return deck.getLocation();
	}

	@Override
	public void clear() {
		this.deck = null;
	}

	@Override
	public void addAll(ICardStore store) {
		setStore(store);
	}
}
