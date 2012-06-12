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
package com.reflexit.magiccards.ui.views.collector;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.AbstractMultiStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.AbstractCardsView;

/**
 * Shows sets and how many cards collected per set
 * 
 */
public class CollectorView extends AbstractCardsView implements ISelectionListener {
	public static final String ID = CollectorView.class.getName();
	private Action delete;
	private Action refresh;
	private MemoryFilteredCardStore<IMagicCard> fstore = new MemoryFilteredCardStore<IMagicCard>();

	/**
	 * The constructor.
	 */
	public CollectorView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		loadInitial();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, MagicUIActivator.helpId("viewcollector"));
	}

	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		ActionHandler deleteHandler = new ActionHandler(this.delete);
		IHandlerService service = (IHandlerService) (getSite()).getService(IHandlerService.class);
		service.activateHandler("org.eclipse.ui.edit.delete", deleteHandler);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(refresh);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		// manager.add(PerspectiveFactoryMagic.createNewMenu(getViewSite().getWorkbenchWindow()));
		// drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.delete = new Action("Delete") {
			@Override
			public void run() {
				actionDelete();
			}
		};
		this.refresh = new Action("Refresh", SWT.NONE) {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/refresh.gif"));
			}

			@Override
			public void run() {
				updateViewer();
			}
		};
	}

	@Override
	protected void populateStore(IProgressMonitor monitor) {
		super.populateStore(monitor);
		fstore.clear();
		ICardStore lib = DataManager.getCardHandler().getLibraryFilteredStore().getCardStore();
		// ICardStore magicDB = DataManager.getCardHandler().getMagicDBStore();
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(lib.size());
		// for (Iterator iterator = magicDB.iterator(); iterator.hasNext();) {
		// IMagicCard card = (IMagicCard) iterator.next();
		// list.add(card);
		// }
		for (Iterator iterator = lib.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			list.add(card);
		}
		getFilteredStore().getCardStore().addAll(list);
		getFilteredStore().update(getFilter());
		CardGroup[] elements = getFilteredStore().getCardGroups();
		for (int i = 0; i < elements.length; i++) {
			CardGroup cardGroup = elements[i];
			// suppose to be groupped by set
			IMagicCard firstCard = cardGroup.getFirstCard();
			String set = firstCard == null ? cardGroup.getName() : firstCard.getSet();
			Location loc = Location.createLocationFromSet(set);
			ICardStore<IMagicCard> store = ((AbstractMultiStore<IMagicCard>) DataManager.getCardHandler().getMagicDBStore()).getStore(loc);
			for (Iterator iterator = store.iterator(); iterator.hasNext();) {
				IMagicCard card = (IMagicCard) iterator.next();
				if (!cardGroup.contains(card)) {
					System.err.println("Does not contain " + card);
					getFilteredStore().getCardStore().add(card);
				}
			}
		}
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		return fstore;
	}

	/**
	 *
	 */
	protected void actionDelete() {
		// TODO
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getPage().addSelectionListener(this);
	}

	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(this);
		super.dispose();
	}

	protected void loadInitial() {
		// TOTO remove?
	}

	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		// TODO remove?
	}

	@Override
	protected CollectorListControl doGetViewControl() {
		return new CollectorListControl(this);
	}

	@Override
	protected String getPreferencePageId() {
		return null;
	}

	@Override
	public String getId() {
		return ID;
	}
}