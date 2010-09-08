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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.model.storage.PlayingDeck;
import com.reflexit.magiccards.ui.preferences.DeckViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.LazyTableViewerManager;
import com.reflexit.magiccards.ui.views.ViewerManager;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

/**
 * @author Alena
 *
 */
public class HandView extends AbstractCardsView implements ISelectionListener {
	public static final String ID = HandView.class.getName();
	protected PlayingDeck store = new PlayingDeck(new MemoryCardStore());
	private IAction shuffle;
	private Action draw;

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#doGetFilteredStore()
	 */
	@Override
	public IFilteredCardStore doGetFilteredStore() {
		return this.store;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#doGetViewerManager(com.reflexit.magiccards.ui.views.AbstractCardsView)
	 */
	@Override
	public ViewerManager doGetViewerManager(AbstractCardsView abstractCardsView) {
		return new LazyTableViewerManager(this);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#fillLocalPullDown(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.shuffle);
		manager.add(this.draw);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#makeActions()
	 */
	@Override
	protected void makeActions() {
		super.makeActions();
		this.shuffle = new Action("Shuffle") {
			@Override
			public void run() {
				getManager().updateSortColumn(-1);
				HandView.this.store.shuffle();
				HandView.this.store.draw(7);
				getManager().loadData(updateViewerRunnable);
			}
		};
		this.draw = new Action("Draw") {
			@Override
			public void run() {
				HandView.this.store.draw(1);
				getManager().loadData(updateViewerRunnable);
			}
		};
	}

	@Override
	protected String getPrefenceColumnsId() {
		return PreferenceConstants.DECKVIEW_COLS;
	}

	@Override
	protected String getPreferencePageId() {
		return DeckViewPreferencePage.class.getName();
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getPage().addSelectionListener(CardsNavigatorView.ID, this);
	}

	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(CardsNavigatorView.ID, this);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection sel = (IStructuredSelection) selection;
		if (sel.getFirstElement() instanceof CardCollection) {
			CardCollection deck = (CardCollection) sel.getFirstElement();
			if (deck.isOpen()) {
				ICardStore<IMagicCard> store2 = deck.getStore();
				this.store.setStore(store2);
				getManager().updateSortColumn(-1);
				HandView.this.store.shuffle();
				HandView.this.store.draw(7);
				getManager().loadData(updateViewerRunnable);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#loadInitial()
	 */
	@Override
	protected void loadInitial() {
		setStatus("To populate this view use 'Emulate Draw' command from a Deck view");
		super.loadInitial();
	}
}
