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
package com.reflexit.magiccards.ui.views.nav;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.Deck;
import com.reflexit.magiccards.core.model.nav.DecksContainer;
import com.reflexit.magiccards.core.model.nav.MagicDbContainter;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.LibView;

public class CardsNavigatorView extends ViewPart implements ICardEventListener {
	public static final String ID = CardsNavigatorView.class.getName();
	private Action doubleClickAction;
	private CardsNavigatiorManager manager;
	private Action addNewDeck;
	private Action removeDeck;

	/**
	 * The constructor.
	 */
	public CardsNavigatorView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		composite.setLayout(gl);
		createTable(composite);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void createTable(Composite parent) {
		this.manager = new CardsNavigatiorManager(this);
		Control control = this.manager.createContents(parent);
		((Composite) control).setLayoutData(new GridData(GridData.FILL_BOTH));
		// ADD the JFace Viewer as a Selection Provider to the View site.
		getSite().setSelectionProvider(this.manager.getViewer());
	}

	public ColumnViewer getViewer() {
		return this.manager.getViewer();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				CardsNavigatorView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(getViewer().getControl());
		getViewer().getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, getViewer());
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.addNewDeck);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(this.addNewDeck);
		manager.add(this.removeDeck);
		this.removeDeck.setEnabled(canRemove());
		manager.add(new Separator());
		// drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * @return
	 */
	private boolean canRemove() {
		IStructuredSelection sel = (IStructuredSelection) getViewSite().getSelectionProvider().getSelection();
		if (sel.isEmpty())
			return false;
		for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
			CardElement el = (CardElement) iterator.next();
			if (!(el instanceof Deck))
				return false;
		}
		return true;
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(new Separator());
		// drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		// double cick
		this.doubleClickAction = new Action() {
			@Override
			public void run() {
				runDoubleClick();
			}
		};
		this.addNewDeck = new Action("Add New Deck...") {
			@Override
			public void run() {
				addNewDeck();
			}
		};
		this.removeDeck = new Action("Remove Deck") {
			@Override
			public void run() {
				removeDeck();
			}
		};
	}

	/**
	 * 
	 */
	protected void removeDeck() {
		IStructuredSelection sel = (IStructuredSelection) getViewSite().getSelectionProvider().getSelection();
		if (sel.isEmpty())
			return;
		if (sel.size() == 1) {
			Deck el = (Deck) sel.getFirstElement();
			if (MessageDialog.openQuestion(getShell(), "Deck Removal Confirmation",
			        "Are you sure you want to delete deck: " + el.getName() + "?")) {
				((DecksContainer) el.getParent()).removeDeck(el);
			}
		} else {
			if (MessageDialog.openQuestion(getShell(), "Decks Removal Confirmation",
			        "Are you sure you want to delete these " + sel.size() + " decks?")) {
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					CardElement el = (CardElement) iterator.next();
					((DecksContainer) el.getParent()).removeDeck((Deck) el);
				}
			}
		}
	}

	protected void addNewDeck() {
		DecksContainer parent = getDeckContainer();
		InputDialog inputDialog = new InputDialog(getShell(), "Enter name", "Enter a name for a Deck", "", null);
		if (inputDialog.open() == InputDialog.OK) {
			String filename = inputDialog.getValue() + ".xml";
			Deck d = parent.addDeck(filename);
			try {
				openDeckView(d);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private DecksContainer getDeckContainer() {
		ISelection selection = this.manager.getViewer().getSelection();
		DecksContainer parent = DataManager.getModelRoot().getDeckContainer();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) selection;
			if (!iss.isEmpty()) {
				CardElement el = (CardElement) iss.getFirstElement();
				if (el instanceof DecksContainer) {
					parent = (DecksContainer) el;
				} else if (el instanceof Deck) {
					parent = (DecksContainer) ((Deck) el).getParent();
				}
			}
		}
		return parent;
	}

	private void hookDoubleClickAction() {
		getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				CardsNavigatorView.this.doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(getViewSite().getShell(), "Magic Cards", message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		DataManager.getModelRoot().addListener(this);
	}

	@Override
	public void dispose() {
		DataManager.getModelRoot().removeListener(this);
		this.manager.dispose();
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		getViewer().getControl().setFocus();
	}

	protected void runDoubleClick() {
		ISelection selection = getViewer().getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj instanceof CollectionsContainer) {
			try {
				getViewSite().getWorkbenchWindow().getActivePage().showView(LibView.ID);
			} catch (PartInitException e) {
				MagicUIActivator.log(e);
			}
		} else if (obj instanceof MagicDbContainter) {
			try {
				getViewSite().getWorkbenchWindow().getActivePage().showView(MagicDbView.ID);
			} catch (PartInitException e) {
				MagicUIActivator.log(e);
			}
		} else if (obj instanceof Deck) {
			try {
				Deck d = (Deck) obj;
				openDeckView(d);
			} catch (PartInitException e) {
				MagicUIActivator.log(e);
			}
		} else {
			showMessage("Cannot open this object " + obj.toString());
		}
	}

	protected void openDeckView(Deck d) throws PartInitException {
		getViewSite().getWorkbenchWindow().getActivePage().showView(DeckView.ID, d.getFileName(),
		        IWorkbenchPage.VIEW_ACTIVATE);
	}

	public Shell getShell() {
		return getViewSite().getShell();
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.events.ICardEventListener#handleEvent(com.reflexit.magiccards.core.model.events.CardEvent)
	 */
	public void handleEvent(CardEvent event) {
		this.manager.getViewer().refresh();
	}
}