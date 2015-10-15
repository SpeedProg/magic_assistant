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
import com.reflexit.magiccards.core.model.MagicCardPhysical;

/**
 * @author Alena
 * 
 */
public class HashCollectionPart {
	private transient HashMap<Integer, Object> hash;

	public HashCollectionPart() {
		this.hash = new HashMap<Integer, Object>();
	}

	/**
	 * @param key
	 * @return first card in the list
	 */
	public synchronized IMagicCard getCard(Integer key) {
		Object obj = this.hash.get(key);
		if (obj == null) {
			return null;
		}
		if (obj instanceof IMagicCard) {
			return (IMagicCard) obj;
		}
		if (obj instanceof LinkedList) {
			LinkedList<IMagicCard> linkedList = (LinkedList<IMagicCard>) obj;
			for (Iterator iterator = linkedList.iterator(); iterator.hasNext();) {
				IMagicCard card = (IMagicCard) iterator.next();
				return card;
			}
		}
		return null;
	}

	public synchronized IMagicCard getCard(IMagicCard card) {
		Object obj = this.hash.get(card.getCardId());
		if (obj == null) {
			return null;
		}
		if (obj instanceof IMagicCard) {
			return getMatching(card, (IMagicCard) obj);
		}
		if (obj instanceof LinkedList) {
			LinkedList<IMagicCard> linkedList = (LinkedList<IMagicCard>) obj;
			for (Iterator iterator = linkedList.iterator(); iterator.hasNext();) {
				IMagicCard card2 = (IMagicCard) iterator.next();
				card2 = getMatching(card, card2);
				if (card2 != null)
					return card2;
			}
		}
		return null;
	}

	protected IMagicCard getMatching(IMagicCard card, IMagicCard card2) {
		if (card2 instanceof MagicCardPhysical && card instanceof MagicCardPhysical) {
			MagicCardPhysical phi2 = (MagicCardPhysical) card2;
			MagicCardPhysical phi1 = (MagicCardPhysical) card;
			if (!phi1.matching(phi2))
				return null;
			return card2;
		} else if (card2.equals(card))
			return card2;
		return null;
	}

	/**
	 * @param card
	 */
	public synchronized void storeCard(IMagicCard card) {
		Object obj = this.hash.get(card.getCardId());
		if (obj == null) {
			this.hash.put(card.getCardId(), card);
			return;
		}
		LinkedList<IMagicCard> linkedList;
		if (obj instanceof IMagicCard) {
			linkedList = new LinkedList<IMagicCard>();
			linkedList.add((IMagicCard) obj);
			linkedList.add(card);
			this.hash.put(card.getCardId(), linkedList);
			return;
		}
		if (obj instanceof LinkedList) {
			linkedList = (LinkedList<IMagicCard>) obj;
			linkedList.add(card);
			return;
		}
	}

	/**
	 * @param card
	 */
	public synchronized void removeCard(IMagicCard card) {
		Object obj = this.hash.get(card.getCardId());
		if (obj == null) {
			return;
		}
		if (obj instanceof IMagicCard) {
			this.hash.remove(card.getCardId());
			return;
		}
		if (obj instanceof LinkedList) {
			LinkedList<IMagicCard> linkedList = (LinkedList<IMagicCard>) obj;
			linkedList.remove(card);
			if (linkedList.size() == 0) {
				this.hash.remove(card.getCardId());
			}
		}
	}

	/**
	 * @param id
	 * @return
	 */
	public synchronized Collection<IMagicCard> getCards(int id) {
		Object obj = this.hash.get(id);
		if (obj == null) {
			return null;
		}
		if (obj instanceof IMagicCard) {
			LinkedList<IMagicCard> linkedList = new LinkedList<IMagicCard>();
			linkedList.add((IMagicCard) obj);
			return linkedList;
		}
		if (obj instanceof LinkedList) {
			LinkedList<IMagicCard> linkedList = (LinkedList<IMagicCard>) obj;
			return linkedList;
		}
		return null;
	}
}
