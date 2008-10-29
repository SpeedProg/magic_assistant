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
package com.reflexit.magiccards.core.model.utils;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.ICardStore;

/**
 * @author Alena
 *
 */
public class CardStoreUtils {
	public static CardStoreUtils getInstance() {
		if (instance == null)
			instance = new CardStoreUtils();
		return instance;
	}
	public static CardStoreUtils instance;

	/**
	 * mana curve is array 0 .. 8 of card counts,
	 * where non-land is counted, 
	 * arr[8] - is cards with X cost in it, 
	 * arr[7] - is 7+
	 * @param store
	 * @return mana curve for given store
	 */
	public int[] buildManaCurve(ICardStore store) {
		int bars[] = new int[9];
		for (Iterator iterator = store.cardsIterator(); iterator.hasNext();) {
			IMagicCard elem = (IMagicCard) iterator.next();
			int cost = elem.getCmc();
			if (elem.getCost().length() == 0)
				continue; // land
			int count;
			if (elem instanceof ICardCountable) {
				count = ((ICardCountable) elem).getCount();
			} else {
				count = 1;
			}
			if (elem.getCost().contains("X"))
				bars[8] += count;
			else if (cost < 7 && cost >= 0)
				bars[cost] += count;
			else if (cost > 7)
				bars[7] += count;
			else
				System.err.println("mana curve: cost:" + elem.getCost());
		}
		return bars;
	}

	private CardStoreUtils() {
	}
}
