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
package com.reflexit.magiccards.ui.views.analyzers;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.ui.views.AbstractViewPage;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

/**
 * AbstractDeckPage class
 */
public abstract class AbstractDeckPage extends AbstractViewPage implements IDeckPage {
	protected ICardStore store;

	protected IStorageInfo getStorageInfo() {
		IStorage storage = getCardStore().getStorage();
		if (storage instanceof IStorageInfo) {
			IStorageInfo si = ((IStorageInfo) storage);
			return si;
		}
		return null;
	}

	public DeckView getDeckView() {
		return (DeckView) getViewPart();
	}

	@Override
	public void refresh() {
		getCardStore(); // init card store
	}

	public ICardStore<IMagicCard> getCardStore() {
		if (store == null && getViewPart() != null && getDeckView().getCardCollection() != null)
			store = getDeckView().getCardCollection().getStore();
		return store;
	}

	protected int getCount(Object element) {
		if (element == null)
			return 0;
		int count = ((element instanceof ICardCountable) ? ((ICardCountable) element).getCount() : 1);
		return count;
	}
}
