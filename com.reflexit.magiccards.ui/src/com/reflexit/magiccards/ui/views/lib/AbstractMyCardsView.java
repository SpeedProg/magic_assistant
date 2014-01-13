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
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
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
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dialogs.EditCardsPropertiesDialog;
import com.reflexit.magiccards.ui.dialogs.SplitDialog;
import com.reflexit.magiccards.ui.exportWizards.ExportAction;
import com.reflexit.magiccards.ui.views.AbstractCardsView;

/**
 * Cards view for personal cards (decks and collections)
 * 
 * @author Alena
 * 
 */
public abstract class AbstractMyCardsView extends AbstractCardsView implements ICardEventListener {
	protected Action delete;
	private Action split;
	private Action edit;
	protected Action export;
	private MenuManager moveToDeckMenu;
	private MenuManager addToDeck;
	protected IDeckAction copyToDeck;

	@Override
	protected void makeActions() {
		super.makeActions();
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		this.delete = new Action("Remove") {
			@Override
			public void run() {
				removeSelected();
			}
		};
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
			public void menuAboutToShow(IMenuManager manager) {
				fillDeckMenu(manager, moveToDeck);
			}
		});
		this.addToDeck = new MenuManager("Copy to");
		this.addToDeck.setRemoveAllWhenShown(true);
		this.addToDeck.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillDeckMenu(manager, copyToDeck);
			}
		});
		this.export = createExportAction();
		copyToDeck = new IDeckAction() {
			public void run(String id) {
				IFilteredCardStore fstore = DataManager.getCardHandler().getCardCollectionFilteredStore(id);
				Location loc = fstore.getLocation();
				ISelection selection = getSelectionProvider().getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					if (!sel.isEmpty()) {
						DataManager.copyCards(sel.toList(), loc);
					}
				}
			}
		};
	}

	protected ExportAction createExportAction() {
		return new ExportAction(new StructuredSelection(), getPreferencePageId());
	}

	protected IDeckAction moveToDeck = new IDeckAction() {
		public void run(String id) {
			try {
				ISelection selection = getSelectionProvider().getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					if (!sel.isEmpty()) {
						ArrayList<IMagicCard> list = new ArrayList<IMagicCard>();
						for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
							Object o = iterator.next();
							if (o instanceof IMagicCard)
								list.add((IMagicCard) o);
						}
						Location location = DataManager.getCardHandler().getCardCollectionFilteredStore(id).getCardStore().getLocation();
						DataManager.moveCards(list, location);
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
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof MagicCardPhysical) {
						((MagicCardPhysical) o).setOwn(b);
						DataManager.update((MagicCardPhysical) o);
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
				DataManager.remove(cardStore, sel.toList());
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
						DataManager.split(card, left, right);
					}
				}
			}
		}
	}

	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		super.setGlobalHandlers(bars);
		activateActionHandler(delete, "org.eclipse.ui.edit.delete");
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		super.fillLocalPullDown(manager);
		manager.add(this.export);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#fillContextMenu(org
	 * .eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(this.actionCopy);
		manager.add(this.actionPaste);
		manager.add(this.moveToDeckMenu);
		manager.add(this.addToDeck);
		manager.add(this.split);
		manager.add(this.edit);
		manager.add(this.showInstances);
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	}

	@Override
	protected void runDoubleClick() {
		edit.run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#init(org.eclipse.ui .IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		if (DataManager.waitForInit()) {
			DataManager.getLibraryCardStore().addListener(AbstractMyCardsView.this);
			DataManager.getModelRoot().addListener(AbstractMyCardsView.this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#dispose()
	 */
	@Override
	public void dispose() {
		DataManager.getCardHandler().getLibraryFilteredStore().getCardStore().removeListener(this);
		DataManager.getModelRoot().removeListener(this);
		super.dispose();
	}

	public void handleEvent(final CardEvent event) {
		int type = event.getType();
		if (type == CardEvent.UPDATE) {
			// handled by card control
		} else if (type == CardEvent.ADD_CONTAINER || type == CardEvent.REMOVE_CONTAINER) {
			// nothing
		} else if (type == CardEvent.ADD) {
			// handled by card control
		} else if (type == CardEvent.RENAME_CONTAINER) {
			reloadData();
		}
	}

	protected void editSelected() {
		final IStructuredSelection selection = (IStructuredSelection) getSelectionProvider().getSelection();
		if (selection.isEmpty())
			return;
		final PreferenceStore store = new PreferenceStore();
		boolean first = true;
		boolean any = false;
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof MagicCardPhysical) {
				any = true;
				MagicCardPhysical card = (MagicCardPhysical) object;
				if (first) {
					ICardField[] allFields = MagicCardFieldPhysical.allFields();
					for (ICardField f : allFields) {
						Object value = card.getObjectByField(f);
						String svalue = String.valueOf(value == null ? "" : value);
						store.setDefault(f.name(), svalue);
					}
					first = false;
				} else {
					ICardField[] allFields = MagicCardFieldPhysical.allFields();
					for (ICardField f : allFields) {
						Object value = card.getObjectByField(f);
						String svalue = String.valueOf(value == null ? "" : value);
						if (!svalue.equals(store.getDefaultString(f.name()))) {
							store.setDefault(f.name(), UNCHANGED);
						}
					}
					store.setDefault(EditCardsPropertiesDialog.NAME_FIELD, "<Multiple Cards>: " + selection.size());
				}
			}
		}
		if (any) {
			new EditCardsPropertiesDialog(getViewSite().getShell(), store).open();
			new Job("Updating edited cards") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					int i = 0;
					int len = selection.size();
					for (Iterator iterator = selection.iterator(); iterator.hasNext(); i++) {
						MagicCardPhysical card = (MagicCardPhysical) iterator.next();
						editCard(card, store, false);
						if (i % 1000 == 0 || i == len - 1) {
							DataManager.update(card); // XXX
						}
					}
					DataManager.reconcile();
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	private final static String UNCHANGED = EditCardsPropertiesDialog.UNCHANGED;

	private void editCard(MagicCardPhysical card, PreferenceStore store, boolean update) {
		boolean modified = false;
		modified = setField(card, store, MagicCardFieldPhysical.COUNT) || modified;
		modified = setField(card, store, MagicCardFieldPhysical.FORTRADECOUNT) || modified;
		modified = setField(card, store, MagicCardFieldPhysical.PRICE) || modified;
		modified = setField(card, store, MagicCardFieldPhysical.COMMENT) || modified;
		modified = setField(card, store, MagicCardFieldPhysical.OWNERSHIP) || modified;
		String special = card.getSpecial();
		String especial = store.getString(EditCardsPropertiesDialog.SPECIAL_FIELD);
		if (!UNCHANGED.equals(especial) && !especial.equals(special)) {
			card.setSpecial(especial);
			modified = true;
		}
		if (modified && update) {
			DataManager.update(card);
		}
	}

	protected boolean setField(MagicCardPhysical card, PreferenceStore store, ICardField field) {
		Boolean modified = false;
		String orig = String.valueOf(card.getObjectByField(field));
		String edited = store.getString(field.name());
		if (!UNCHANGED.equals(edited) && !edited.equals(orig)) {
			try {
				card.setObjectByField(field, edited);
				modified = true;
			} catch (Exception e) {
				// was bad value
				MessageDialog.openError(getShell(), "Error", "Invalid value for " + field + ": " + edited);
			}
		}
		return modified;
	}
}
