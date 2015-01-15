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

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.storage.ICardEventManager;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

/**
 * AbstractDeckPage class
 */
public class AbstractDeckPage implements IDeckPage {
	protected DeckView view;
	protected ICardStore store;
	private Composite area;

	@Override
	public Composite createContents(Composite parent) {
		area = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		area.setLayout(layout);
		return area;
	}

	@Override
	public Control getControl() {
		return getArea();
	}

	public Composite getArea() {
		return area;
	}

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

	@Override
	public void setDeckView(DeckView view) {
		this.view = view;
	}

	@Override
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
		// global handlers
		setGlobalControlHandlers(bars);
		bars.updateActionBars();
		// selection provider
		view.getSelectionProvider().setSelectionProviderDelegate(getSelectionProvider());
		getCardStore();
	}

	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	/**
	 * @param bars
	 */
	public void setGlobalControlHandlers(IActionBars bars) {
		// override if needed
	}

	protected MenuManager hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		return menuMgr;
	}

	/**
	 * @param viewMenuManager
	 */
	public void fillContextMenu(IMenuManager viewMenuManager) {
		// override if need view menu
	}

	/**
	 * @param viewMenuManager
	 */
	public void fillLocalPullDown(IMenuManager viewMenuManager) {
		// override if need view menu
	}

	/**
	 * @param toolBarManager
	 */
	public void fillLocalToolBar(IToolBarManager toolBarManager) {
		// override if need toolbar
	}

	@Override
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

	protected int getCount(Object element) {
		if (element == null)
			return 0;
		int count = ((element instanceof ICardCountable) ? ((ICardCountable) element).getCount() : 1);
		return count;
	}

	@Override
	public void dispose() {
		area.dispose();
	}
}
