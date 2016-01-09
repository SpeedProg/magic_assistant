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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.storage.ICardEventManager;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.ui.views.AbstractViewPage;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

/**
 * AbstractDeckPage class
 */
public class AbstractDeckPage extends AbstractViewPage implements IDeckPage {
	protected ICardStore store;

	@Override
	public String getStatusMessage() {
		ICardEventManager cardStore = store;
		String cardCountTotal = "";
		if (cardStore instanceof ICardCountable) {
			cardCountTotal = "Total cards: " + ((ICardCountable) cardStore).getCount();
		}
		return cardCountTotal;
	}

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
	public void activate() {
		super.activate();
		// selection provider
		// XXX
		getCardStore();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	@Override
	public void setFilteredStore(IFilteredCardStore store) {
		this.store = store.getCardStore();
	}

	public ICardStore<IMagicCard> getCardStore() {
		if (store == null && getViewPart() != null && getDeckView().getCardCollection() != null)
			store = getDeckView().getCardCollection().getStore();
		return store;
	}

	protected Label createStatusLine(Composite composite) {
		Label statusLine = new Label(composite, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 3;
		statusLine.setLayoutData(gd);
		statusLine.setText("Status");
		return statusLine;
	}

	protected int getCount(Object element) {
		if (element == null)
			return 0;
		int count = ((element instanceof ICardCountable) ? ((ICardCountable) element).getCount() : 1);
		return count;
	}
}
