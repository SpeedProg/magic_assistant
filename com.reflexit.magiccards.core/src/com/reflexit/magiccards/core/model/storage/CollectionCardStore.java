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
import java.util.Iterator;

import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;

/**
 * @author Alena
 *
 */
public class CollectionCardStore extends AbstractCardStoreWithStorage<IMagicCard> implements
        ICardCollection<IMagicCard>, IStorageContainer<IMagicCard> {
	protected HashCollectionPart hashpart;
	protected int cardCount;

	public CollectionCardStore(IStorage<IMagicCard> storage) {
		this(storage, true);
	}

	public CollectionCardStore(IStorage<IMagicCard> storage, boolean wrapped) {
		super(storage, wrapped);
		this.hashpart = new HashCollectionPart();
	}

	@Override
	public boolean doAddCard(IMagicCard card) {
		if (getMergeOnAdd()) {
			MagicCardPhisical phi = (MagicCardPhisical) this.hashpart.getCard(card);
			String loc = null;
			if (this instanceof ILocatable)
				loc = ((ILocatable) this).getLocation();
			if (phi == null || loc != null && !loc.equals(phi.getLocation())) {
				phi = (MagicCardPhisical) doAddCardNoMerge(card);
				if (phi.getLocation() == null)
					phi.setLocation(loc);
				this.hashpart.storeCard(phi);
				if (this.storage.add(phi))
					return true;
				else {
					this.hashpart.removeCard(phi);
					return false;
				}
			} else {
				int count = 1;
				if (card instanceof ICardCountable) {
					count = ((ICardCountable) card).getCount();
				}
				MagicCardPhisical add = new MagicCardPhisical(card);
				if (add.getLocation() == null)
					add.setLocation(loc);
				MagicCardPhisical old = phi;
				add.setCount(old.getCount() + count);
				doRemoveCard(old);
				this.cardCount += add.getCount();
				this.hashpart.storeCard(add);
				if (storageAdd(add))
					return true;
				else {
					return false;
				}
			}
		}
		IMagicCard phi = doAddCardNoMerge(card);
		this.hashpart.storeCard(phi);
		if (storageAdd(phi))
			return true;
		else {
			return false;
		}
	}

	private boolean storageAdd(IMagicCard card) {
		return this.storage.add(card);
	}

	protected IMagicCard doAddCardNoMerge(IMagicCard card) {
		IMagicCard phi;
		int count = 1;
		if (card instanceof MagicCardPhisical) {
			phi = new MagicCardPhisical(card);
			count = ((MagicCardPhisical) card).getCount();
			((MagicCardPhisical) phi).setCount(count);
		} else {
			phi = new MagicCardPhisical(card);
		}
		this.cardCount += count;
		return phi;
	}

	@Override
	public boolean doRemoveCard(IMagicCard card) {
		if (!(card instanceof MagicCardPhisical))
			return false;
		MagicCardPhisical phi = (MagicCardPhisical) card;
		Collection cards = this.hashpart.getCards(card.getCardId());
		MagicCardPhisical found = null;
		MagicCardPhisical max = null;
		if (cards != null)
			for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
				MagicCardPhisical candy = (MagicCardPhisical) iterator.next();
				if (phi.matching(candy)) {
					if (phi.getCount() == candy.getCount()) {
						found = candy;
						break;
					}
					if (max == null || max.getCount() < candy.getCount())
						max = candy;
				}
			}
		if (found != null) {
			storageRemove(found);
			this.hashpart.removeCard(found);
			this.cardCount -= found.getCount();
			return true;
		} else {
			if (max == null)
				return false;
			if (max.getCount() < phi.getCount())
				return false;
			MagicCardPhisical add = new MagicCardPhisical(max);
			add.setCount(max.getCount() - phi.getCount());
			storageRemove(max);
			this.hashpart.removeCard(max);
			this.cardCount -= max.getCount();
			doAddCard(add);
			return true;
		}
	}

	private void storageRemove(IMagicCard card) {
		this.storage.remove(card);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.xml.SingleFileCardStore#doInitialize()
	 */
	@Override
	protected synchronized void doInitialize() {
		cardCount = 0;
		this.hashpart = new HashCollectionPart();
		this.storage.load();
		// load in hash
		for (Object element : this) {
			IMagicCard card = (IMagicCard) element;
			this.hashpart.storeCard(card);
			if (card instanceof ICardCountable) {
				this.cardCount += ((ICardCountable) card).getCount();
			} else {
				this.cardCount += 1;
			}
		}
	}

	public IMagicCard getCard(int id) {
		return this.hashpart.getCard(id);
	}

	public Collection getCards(int id) {
		return this.hashpart.getCards(id);
	}

	public int getCount() {
		return this.cardCount;
	}

	public void clear() {
		cardCount = 0;
		this.hashpart = new HashCollectionPart();
	}
}
