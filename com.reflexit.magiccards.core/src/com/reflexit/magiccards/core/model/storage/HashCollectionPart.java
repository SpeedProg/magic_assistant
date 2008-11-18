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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;

/**
 * @author Alena
 *
 */
public class HashCollectionPart {
	private transient HashMap<Integer, LinkedList<IMagicCard>> hash;

	public HashCollectionPart() {
		this.hash = new HashMap<Integer, LinkedList<IMagicCard>>();
	}

	/**
	 * @param key
	 * @return first card in the list
	 */
	public IMagicCard getCard(Integer key) {
		LinkedList<IMagicCard> linkedList = this.hash.get(key);
		if (linkedList == null)
			return null;
		for (Iterator iterator = linkedList.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			return card;
		}
		return null;
	}

	public IMagicCard getCard(IMagicCard card) {
		LinkedList<IMagicCard> linkedList = this.hash.get(card.getCardId());
		if (linkedList == null)
			return null;
		for (Iterator iterator = linkedList.iterator(); iterator.hasNext();) {
			IMagicCard card2 = (IMagicCard) iterator.next();
			if (card2 instanceof MagicCardPhisical && card instanceof MagicCardPhisical) {
				MagicCardPhisical phi2 = (MagicCardPhisical) card2;
				MagicCardPhisical phi1 = (MagicCardPhisical) card;
				if (!phi1.matching(phi2))
					continue;
				return card2;
			} else
				return card2;
		}
		return null;
	}

	/**
	 * @param card
	 */
	public void storeCard(IMagicCard card) {
		LinkedList<IMagicCard> linkedList = this.hash.get(card.getCardId());
		if (linkedList == null) {
			linkedList = new LinkedList<IMagicCard>();
			this.hash.put(card.getCardId(), linkedList);
		}
		linkedList.add(card);
	}

	/**
	 * @param card
	 */
	public void removeCard(IMagicCard card) {
		LinkedList<IMagicCard> linkedList = this.hash.get(card.getCardId());
		if (linkedList == null) {
			return;
		}
		linkedList.remove(card);
		if (linkedList.size() == 0) {
			this.hash.remove(card.getCardId());
		}
	}

	/**
	 * @param id
	 * @return
	 */
	public Collection getCards(int id) {
		LinkedList<IMagicCard> linkedList = this.hash.get(id);
		return linkedList; // TODO: xxx unsafe
	}
}
