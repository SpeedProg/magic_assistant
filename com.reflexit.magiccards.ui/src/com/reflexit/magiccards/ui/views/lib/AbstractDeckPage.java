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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;

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
		// toolbar
		IActionBars bars = view.getViewSite().getActionBars();
		IToolBarManager toolBarManager = bars.getToolBarManager();
		toolBarManager.removeAll();
		fillLocalToolBar(toolBarManager);
		toolBarManager.update(true);
		// local view menu
		IMenuManager viewMenuManager = bars.getMenuManager();
		viewMenuManager.removeAll();
		fillLocalPullDown(viewMenuManager);
		viewMenuManager.updateAll(true);
		getCardStore();
		if (store == null)
			return;
	}

	protected void fillLocalPullDown(IMenuManager viewMenuManager) {
		// override if need view menu
	}

	protected void fillLocalToolBar(IToolBarManager toolBarManager) {
		// override if need toolbar
	}

	public void setFilteredStore(IFilteredCardStore store) {
		this.store = store.getCardStore();
	}

	public ICardStore<IMagicCard> getCardStore() {
		if (store == null && view != null && view.getCardCollection() != null)
			store = view.getCardCollection().getStore();
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
}
