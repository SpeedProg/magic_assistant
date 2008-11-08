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

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;

/**
 * @author Alena
 *
 */
public class RandomDeck extends AbstractFilteredCardStore<IMagicCard> {
	ICardStore store;

	/**
	 * 
	 */
	public RandomDeck(ICardStore store) {
		this.store = store;
	}

	public void setStore(ICardStore store) {
		this.store = store;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.storage.IFilteredCardStore#getCardStore()
	 */
	public ICardStore getCardStore() {
		return this.store;
	}

	@Override
	public Collection<IMagicCard> filterCards(MagicCardFilter filter) throws MagicException {
		return CardStoreUtils.randomize(this.store);
	};
}
