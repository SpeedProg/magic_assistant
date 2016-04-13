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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.actions.DeleteCardAction;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.dialogs.EditMagicCardPhysicalDialog;
import com.reflexit.magiccards.ui.dialogs.MyCardsFilterDialog;
import com.reflexit.magiccards.ui.dialogs.SplitDialog;
import com.reflexit.magiccards.ui.exportWizards.ExportAction;
import com.reflexit.magiccards.ui.views.AbstractGroupPageCardsView;
import com.reflexit.magiccards.ui.views.ViewPageGroup;

/**
 * Cards view for personal cards (decks and collections)
 *
 * @author Alena
 *
 */
public abstract class AbstractMyCardsView extends AbstractGroupPageCardsView implements ICardEventListener {
	private final DataManager DM = DataManager.getInstance();
	private Action delete;
	private Action split;
	private Action edit;
	private ExportAction export;
	private MenuManager moveToDeckMenu;
	private MenuManager addToDeck;
	private IDeckAction copyToDeck;
	private LibraryEventListener eventListener = new LibraryEventListener();

	@Override
	protected ViewPageGroup createPageGroup() {
		return new ViewPageGroup(this::preActivate, this::postActivate);
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		this.delete = new DeleteCardAction(this::removeSelected);
		this.delete.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		this.split = new Action("Split Pile...") {
			@Override
			public void run() {
				splitSelected();
			}
		};
		this.edit = new Action("Edit Card...") {
			@Override
			public void run() {
				editSelected();
			}
		};
		this.moveToDeckMenu = new MenuManager("Move to");
		this.moveToDeckMenu.setRemoveAllWhenShown(true);
		this.moveToDeckMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillDeckMenu(manager, moveToDeck);
			}
		});
		this.addToDeck = new MenuManager("Copy to");
		this.addToDeck.setRemoveAllWhenShown(true);
		this.addToDeck.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillDeckMenu(manager, copyToDeck);
			}
		});
		this.export = createExportAction();
		copyToDeck = new IDeckAction() {
			@Override
			public void run(String id) {
				IFilteredCardStore fstore = DM.getCardHandler().getCardCollectionFilteredStore(id);
				ISelection selection = getSelectionProvider().getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					if (!sel.isEmpty()) {
						DM.copyCards(DM.expandGroups(sel.toList()), fstore.getCardStore());
					}
				}
			}
		};
	}

	protected ExportAction createExportAction() {
		return new ExportAction(new StructuredSelection(), getPreferencePageId());
	}

	protected IDeckAction moveToDeck = new IDeckAction() {
		@Override
		public void run(String id) {
			try {
				ISelection selection = getSelectionProvider().getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					if (!sel.isEmpty()) {
						ICardStore cardStore = DM.getCardHandler().getCardCollectionFilteredStore(id).getCardStore();
						DM.moveCards(DM.expandGroups(sel.toList()), cardStore);
					}
				}
			} catch (MagicException e) {
				MessageDialog.openError(getShell(), "Error", e.getMessage());
			}
		}
	};

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
		ISelection selection = getSelectionProvider().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (!sel.isEmpty()) {
				Set<MagicCardField> of = Collections.singleton(MagicCardField.OWNERSHIP);
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof MagicCardPhysical) {
						((MagicCardPhysical) o).setOwn(b);
						DM.update((MagicCardPhysical) o, of);
					}
				}
			}
		}
	}

	protected void removeSelected() {
		ICardStore cardStore = getFilteredStore().getCardStore();
		ISelection selection = getSelectionProvider().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (!sel.isEmpty()) {
				DM.remove(DM.expandGroups(sel.toList()), cardStore);
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
		ISelection selection = getSelectionProvider().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (!sel.isEmpty()) {
				int max = 0;
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof MagicCardPhysical) {
						MagicCardPhysical card = (MagicCardPhysical) o;
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
				if (type == 0)
					return;
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof MagicCardPhysical) {
						MagicCardPhysical card = (MagicCardPhysical) o;
						int count = card.getCount();
						if (count == 1)
							continue;
						int left = type;
						if (type == EVEN)
							left = count / 2;
						if (left >= count)
							continue;
						int right = count - left;
						DM.split(card, right);
					}
				}
			}
		}
	}

	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		super.setGlobalHandlers(bars);
		activateActionHandler(delete, delete.getActionDefinitionId());
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		super.fillLocalPullDown(manager);
		manager.add(this.export);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.reflexit.magiccards.ui.views.AbstractCardsView#fillContextMenu(org
	 * .eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(this.actionCopy);
		manager.add(this.moveToDeckMenu);
		manager.add(this.addToDeck);
		manager.add(this.split);
		manager.add(this.edit);
		manager.add(this.buyCards);
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
	}

	@Override
	protected void runDoubleClick() {
		edit.run();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		eventListener.init(getViewSite(), this::loadInitialInBackground);
	}

	protected void loadInitialInBackground() {
		// do nothing
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		eventListener.setEventHandler(this::handleEvent);
	}

	@Override
	public void dispose() {
		eventListener.dispose();
		super.dispose();
	}

	protected void editSelected() {
		final IStructuredSelection selection = (IStructuredSelection) getSelectionProvider().getSelection();
		if (selection.isEmpty())
			return;
		ArrayList<MagicCardPhysical> cards = new ArrayList<>();
		DataManager.expandGroups(cards, selection.toList(), (o) -> o instanceof MagicCardPhysical);
		if (!cards.isEmpty())
			new EditMagicCardPhysicalDialog(getViewSite().getShell(), cards).open();
	}

	@Override
	public void handleEvent(CardEvent event) {
		// do nothing
	}

	@Override
	public CardFilterDialog getCardFilterDialog() {
		return new MyCardsFilterDialog(getShell(), getFilterPreferenceStore());
	}
}
