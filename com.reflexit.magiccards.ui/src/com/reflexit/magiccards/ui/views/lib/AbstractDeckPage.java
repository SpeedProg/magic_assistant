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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.core.model.storage.ICardEventManager;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * AbstractDeckPage class
 */
public class AbstractDeckPage implements IDeckPage {
	protected DeckView view;
	protected ICardStore store;
	protected Composite area;

	public Composite createContents(Composite parent) {
		area = new Composite(parent, SWT.NONE);
		return area;
	}

	public Control getControl() {
		return area;
	}

	public String getStatusMessage() {
		return "";
	}

	public void setDeckView(DeckView view) {
		this.view = view;
	}

	public void updateFromStore() {
		// nothing
	}

	public void setFilteredStore(IFilteredCardStore store) {
		this.store = store.getCardStore();
	};

	public ICardEventManager getCardStore() {
		return store;
	}
}
