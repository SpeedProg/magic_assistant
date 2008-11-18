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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.ui.dialogs.SplitDialog;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.CompositeViewerManager;
import com.reflexit.magiccards.ui.views.ViewerManager;

/**
 * @author Alena
 *
 */
public abstract class CollectionView extends AbstractCardsView implements ICardEventListener {
	protected Action delete;
	protected MenuManager ownership;
	private Action split;

	@Override
	protected void makeActions() {
		super.makeActions();
		this.delete = new Action("Remove") {
			@Override
			public void run() {
				removeSelected();
			}
		};
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		this.delete.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		this.ownership = new MenuManager("Ownership");
		this.ownership.setRemoveAllWhenShown(true);
		this.ownership.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillOwnerShipMenu(manager);
			}
		});
		this.split = new Action("Split Pile...") {
			@Override
			public void run() {
				splitSelected();
			}
		};
	}

	protected void fillOwnerShipMenu(IMenuManager manager) {
		manager.add(new Action("Own", SWT.CHECK) {
			@Override
			public void run() {
				changeSelectedOwnerShip(true);
			}
		});
		manager.add(new Action("Not Own", SWT.CHECK) {
			@Override
			public void run() {
				changeSelectedOwnerShip(false);
			}
		});
	}

	/**
	 * @param b
	 */
	protected void changeSelectedOwnerShip(boolean b) {
		ICardStore cardStore = this.manager.getFilteredStore().getCardStore();
		ISelection selection = getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (!sel.isEmpty()) {
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof MagicCardPhisical) {
						((MagicCardPhisical) o).setOwn(b);
						cardStore.updateCard(o);
					}
				}
			}
		}
	}

	protected void removeSelected() {
		ICardStore cardStore = this.manager.getFilteredStore().getCardStore();
		ISelection selection = getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (!sel.isEmpty()) {
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof IMagicCard) {
						cardStore.removeCard(o);
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	protected void splitSelected() {
		final int PICK = 0;
		final int N_TO_1 = 1;
		final int EVEN = -2;
		ICardStore cardStore = this.manager.getFilteredStore().getCardStore();
		ISelection selection = getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (!sel.isEmpty()) {
				int max = 0;
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof MagicCardPhisical) {
						MagicCardPhisical card = (MagicCardPhisical) o;
						int count = card.getCount();
						if (count > max)
							max = count;
					}
				}
				if (max == 1) {
					MessageDialog.openInformation(getShell(), "Split", "Minimum pile, cannot split any further");
					return;
				}
				int type = PICK;
				if (max == 2) {
					type = EVEN;
				} else if (max == 3) {
					type = N_TO_1;
				}
				if (type == PICK) {
					type = SplitDialog.askSplitType(getShell(), max);
				}
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof MagicCardPhisical) {
						MagicCardPhisical card = (MagicCardPhisical) o;
						int count = card.getCount();
						int left = type;
						if (left >= count)
							continue;
						if (type == EVEN)
							left = count / 2;
						MagicCardPhisical card2 = new MagicCardPhisical(card);
						card.setCount(left);
						cardStore.updateCard(card);
						card2.setCount(count - left);
						cardStore.setMergeOnAdd(false);
						cardStore.addCard(card2);
						cardStore.setMergeOnAdd(true);
					}
				}
			}
		}
	}

	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		super.setGlobalHandlers(bars);
		ActionHandler deleteHandler = new ActionHandler(this.delete);
		IHandlerService service = (IHandlerService) (getSite()).getService(IHandlerService.class);
		service.activateHandler("org.eclipse.ui.edit.delete", deleteHandler);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(this.ownership);
		manager.add(this.split);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		this.manager.getFilteredStore().getCardStore().addListener(this);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#dispose()
	 */
	@Override
	public void dispose() {
		this.manager.getFilteredStore().getCardStore().removeListener(this);
		super.dispose();
	}

	@Override
	public ViewerManager doGetViewerManager(AbstractCardsView abstractCardsView) {
		return new CompositeViewerManager(this);
	}

	public void handleEvent(CardEvent event) {
		if (event.getType() == CardEvent.UPDATE) {
			this.manager.getViewer().update(event.getSource(), null);
			getManager().updateStatus();
		} else {
			this.manager.loadData();
		}
	}
}
