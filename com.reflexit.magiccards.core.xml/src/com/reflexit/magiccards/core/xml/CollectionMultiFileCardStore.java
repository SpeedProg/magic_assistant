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
package com.reflexit.magiccards.core.xml;

import java.util.HashMap;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;

/**
 * @author Alena
 *
 */
public class CollectionMultiFileCardStore extends MultiFileCardStore {
	private transient HashMap<Integer, IMagicCard> hash;
	private int cardCount;

	public CollectionMultiFileCardStore() {
		this.hash = new HashMap<Integer, IMagicCard>();
		this.cardCount = 0;
	}

	@Override
	public boolean doAddCard(IMagicCard card) {
		Integer key = card.getCardId();
		IMagicCard phi = this.hash.get(key);
		if (phi == null) {
			phi = new MagicCardPhisical(card);
			super.doAddCard(phi);
			this.hash.put(key, phi);
		} else {
			MagicCardPhisical p = (MagicCardPhisical) phi;
			p.setCount(p.getCount() + 1);
		}
		this.cardCount++;
		return true;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.xml.SingleFileCardStore#doRemoveCard(com.reflexit.magiccards.core.model.IMagicCard)
	 */
	@Override
	public void doRemoveCard(IMagicCard card) {
		super.doRemoveCard(card);
		this.hash.remove(card.getCardId());
		if (card instanceof MagicCardPhisical) {
			this.cardCount -= ((MagicCardPhisical) card).getCount();
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.xml.SingleFileCardStore#doInitialize()
	 */
	@Override
	protected synchronized void doInitialize() {
		super.doInitialize();
		// load in hash
		for (Iterator iterator = cardsIterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			this.hash.put(card.getCardId(), card);
			if (card instanceof MagicCardPhisical) {
				this.cardCount += ((MagicCardPhisical) card).getCount();
			}
		}
	}

	public int getCount() {
		return this.cardCount;
	}
}
