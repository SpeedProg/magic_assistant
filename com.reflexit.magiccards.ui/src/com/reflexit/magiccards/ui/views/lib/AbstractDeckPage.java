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
package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * AbstractDeckPage class
 */
public class AbstractDeckPage implements IDeckPage {
	protected DeckView view;
	protected ICardStore store;
	private Composite area;

	public Composite createContents(Composite parent) {
		area = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		area.setLayout(layout);
		return area;
	}

	public Control getControl() {
		return getArea();
	}

	public Composite getArea() {
		return area;
	}

	public String getStatusMessage() {
		return "";
	}

	public void setDeckView(DeckView view) {
		this.view = view;
	}

	public void activate() {
		view.setTopBarVisible(false);
		getCardStore();
		if (store == null)
			return;
	}

	public void setFilteredStore(IFilteredCardStore store) {
		this.store = store.getCardStore();
	}

	public ICardStore<IMagicCard> getCardStore() {
		if (store == null && view != null && view.getCardCollection() != null)
			store = view.getCardCollection().getStore();
		return store;
	}
}
