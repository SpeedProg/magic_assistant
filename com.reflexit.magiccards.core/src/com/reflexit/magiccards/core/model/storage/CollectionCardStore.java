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
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

/**
 * @author Alena
 * 
 */
public class CollectionCardStore extends AbstractCardStoreWithStorage<IMagicCard> implements ICardStore<IMagicCard>,
		ICardCollection<IMagicCard>, IStorageContainer<IMagicCard> {
	protected HashCollectionPart hashpart;

	public CollectionCardStore(IStorage<IMagicCard> storage) {
		this(storage, true);
	}

	public CollectionCardStore(IStorage<IMagicCard> storage, boolean wrapped) {
		super(storage, wrapped);
		this.hashpart = new HashCollectionPart();
	}

	@Override
	protected boolean doUpdate(IMagicCard card) {
		if (hashpart.getCard(card.getCardId()) == null) {
			// hash if wrong now, fixing
			reindex();
		}
		return super.doUpdate(card);
	}

	@Override
	public boolean doAddCard(IMagicCard card) {
		Location loc = getLocation();
		if (getMergeOnAdd()) {
			MagicCardPhysical phi = (MagicCardPhysical) this.hashpart.getCard(card);
			if (phi == null || loc != null && !loc.equals(phi.getLocation())) {
				phi = (MagicCardPhysical) createNewCard(card, loc);
				this.hashpart.storeCard(phi);
				if (this.storage.add(phi))
					return true;
				else {
					this.hashpart.removeCard(phi);
					return false;
				}
			} else {
				int count = 1;
				int trade = 0;
				if (card instanceof MagicCardPhysical) {
					count = ((ICardCountable) card).getCount();
					trade = ((MagicCardPhysical) card).getForTrade();
				}
				MagicCardPhysical add = new MagicCardPhysical(card, loc);
				MagicCardPhysical old = phi;
				add.setCount(old.getCount() + count);
				add.setForTrade(old.getForTrade() + trade);
				doRemoveCard(old);
				this.hashpart.storeCard(add);
				if (storageAdd(add))
					return true;
				else {
					return false;
				}
			}
		}
		IMagicCard phi = createNewCard(card, loc);
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

	protected IMagicCard createNewCard(IMagicCard card, Location loc) {
		IMagicCard phi;
		int count = 1;
		if (card instanceof MagicCardPhysical) {
			phi = new MagicCardPhysical(card, loc);
			count = ((MagicCardPhysical) card).getCount();
			((MagicCardPhysical) phi).setCount(count);
		} else {
			phi = new MagicCardPhysical(card, loc);
		}
		return phi;
	}

	@Override
	public boolean doRemoveCard(IMagicCard card) {
		if (!(card instanceof MagicCardPhysical))
			return false;
		MagicCardPhysical phi = (MagicCardPhysical) card;
		Collection cards = this.hashpart.getCards(card.getCardId());
		MagicCardPhysical found = null;
		MagicCardPhysical max = null;
		if (cards != null) {
			for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
				MagicCardPhysical candy = (MagicCardPhysical) iterator.next();
				if (phi.matching(candy)) {
					if (phi.getCount() == candy.getCount()) {
						found = candy;
						break;
					}
					if (max == null || max.getCount() < candy.getCount())
						max = candy;
				}
			}
		} else {
			storageRemove(phi);
			return true;
		}
		if (found != null) {
			storageRemove(found);
			this.hashpart.removeCard(found);
			return true;
		} else {
			if (max == null)
				return false;
			if (max.getCount() < phi.getCount())
				return false;
			MagicCardPhysical add = new MagicCardPhysical(max, max.getLocation());
			add.setCount(max.getCount() - phi.getCount());
			storageRemove(max);
			this.hashpart.removeCard(max);
			doAddCard(add);
			return true;
		}
	}

	private void storageRemove(IMagicCard card) {
		this.storage.remove(card);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.xml.SingleFileCardStore#doInitialize()
	 */
	@Override
	protected synchronized void doInitialize() {
		this.storage.load();
		reindex();
	}

	@Override
	public void reindex() {
		this.hashpart = new HashCollectionPart();
		// load in hash
		for (Object element : this) {
			IMagicCard card = (IMagicCard) element;
			this.hashpart.storeCard(card);
		}
	}

	public IMagicCard getCard(int id) {
		return this.hashpart.getCard(id);
	}

	public Collection getCards(int id) {
		return this.hashpart.getCards(id);
	}

	public int getCount() {
		// return this.cardCount;
		return getRealCount();
	}

	public int getRealCount() {
		int count = 0;
		for (Object element : getStorage()) {
			if (element instanceof ICardCountable) {
				ICardCountable card = (ICardCountable) element;
				count += card.getCount();
			} else {
				count++;
			}
		}
		return count;
	}

	public void clear() {
		this.hashpart = new HashCollectionPart();
	}
}