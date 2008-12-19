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

import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardComparator;
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
	static class DrawDeck extends MemoryCardStore implements ICardCountable {
		public int getCount() {
			return size();
		}
	};

	/**
	 * 
	 */
	public PlayingDeck(ICardStore store) {
		setStore(store);
	}

	protected Comparator<IMagicCard> getSortComparator(MagicCardFilter filter) {
	    Comparator<IMagicCard> comp = MagicCardComparator
	            .getComparator(filter.getSortIndex(), filter.isAscending());
	    return comp;
    }
	
	public void setStore(ICardStore store) {
		if (this.deck != store) {
			this.deck = store;
			shuffle();
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.storage.IFilteredCardStore#getCardStore()
	 */
	public ICardStore getCardStore() {
		return this.hand;
	}

	public void draw(int cards) {
		int i = 0;
		for (Iterator iterator = this.library.iterator(); iterator.hasNext() && i < cards; i++) {
			IMagicCard card = (IMagicCard) iterator.next();
			this.hand.add(card);
			iterator.remove();
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
}
