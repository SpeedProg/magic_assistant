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
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.storage.ICardEventManager;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.ui.dialogs.EditCardsPropertiesDialog;
import com.reflexit.magiccards.ui.dialogs.SplitDialog;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.CompositeViewerManager;
import com.reflexit.magiccards.ui.views.ViewerManager;

/**
 * Cards view for personal cards (decks and collections)
 * @author Alena
 *
 */
public abstract class AbstractMyCardsView extends AbstractCardsView implements ICardEventListener {
	protected Action delete;
	private Action split;
	private Action edit;
	private Action paste;
	private MenuManager moveToDeckMenu;

	@Override
	protected void makeActions() {
		super.makeActions();
		this.paste = new Action("Paste") {
			@Override
			public void run() {
				runPaste();
			}
		};
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
		this.moveToDeckMenu = new MenuManager("Move to Deck");
		this.moveToDeckMenu.setRemoveAllWhenShown(true);
		this.moveToDeckMenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillDeckMenu(manager, moveToDeck);
			}
		});
	}
	protected IDeckAction moveToDeck = new IDeckAction() {
		public void run(String id) {
			ISelection selection = getViewer().getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				if (!sel.isEmpty()) {
					ArrayList<IMagicCard> list = new ArrayList<IMagicCard>();
					for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
						Object o = iterator.next();
						if (o instanceof IMagicCard)
							list.add((IMagicCard) o);
					}
					String location = ((ILocatable) DataManager.getCardHandler().getCardCollectionHandler(id)
					        .getCardStore()).getLocation();
					DataManager.getCardHandler().moveCards(list, null, location);
				}
			}
		}
	};

	protected void runPaste() {
		final Clipboard cb = new Clipboard(PlatformUI.getWorkbench().getDisplay());
		MagicCardTransfer mt = MagicCardTransfer.getInstance();
		Object contents = cb.getContents(mt);
		if (contents instanceof IMagicCard[]) {
			IMagicCard[] cards = (IMagicCard[]) contents;
			DataManager.getCardHandler().copyCards(Arrays.asList(cards),
			        ((ILocatable) getFilteredStore().getCardStore()).getLocation());
		}
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
		ICardEventManager cardStore = this.manager.getFilteredStore().getCardStore();
		ISelection selection = getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (!sel.isEmpty()) {
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof MagicCardPhisical) {
						((MagicCardPhisical) o).setOwn(b);
						cardStore.update(o);
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
				cardStore.removeAll(sel.toList());
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
						if (count == 1)
							continue;
						int left = type;
						if (left >= count)
							continue;
						if (type == EVEN)
							left = count / 2;
						MagicCardPhisical card2 = new MagicCardPhisical(card);
						card.setCount(left);
						cardStore.update(card);
						card2.setCount(count - left);
						cardStore.setMergeOnAdd(false);
						cardStore.add(card2);
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
		manager.add(this.copyText);
		manager.add(this.paste);
		manager.add(this.moveToDeckMenu);
		manager.add(this.split);
		manager.add(this.edit);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	}

	@Override
	protected void runDoubleClick() {
		edit.run();
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		this.manager.getFilteredStore().getCardStore().addListener(this);
		DataManager.getModelRoot().addListener(this);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#dispose()
	 */
	@Override
	public void dispose() {
		this.manager.getFilteredStore().getCardStore().removeListener(this);
		DataManager.getModelRoot().removeListener(this);
		super.dispose();
	}

	@Override
	public ViewerManager doGetViewerManager(AbstractCardsView abstractCardsView) {
		return new CompositeViewerManager(this);
	}

	public void handleEvent(final CardEvent event) {
		int type = event.getType();
		if (type == CardEvent.UPDATE) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					manager.getViewer().update(event.getSource(), null);
					updateStatus();
					getViewer().setSelection(new StructuredSelection(event.getSource()), true);
				}
			});
		} else if (type == CardEvent.ADD_CONTAINER || type == CardEvent.REMOVE_CONTAINER) {
			reloadData();
		} else if (type == CardEvent.ADD) {
			if (event.getData() instanceof List) {
				List arr = (List) event.getData();
				if (arr.size() == 1)
					revealSelection = new StructuredSelection(arr);
			} else if (event.getData() instanceof IMagicCard) {
				revealSelection = new StructuredSelection(event.getData());
			}
			reloadData();
		} else {
			reloadData();
		}
	}

	protected void editSelected() {
		IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		if (selection.isEmpty())
			return;
		PreferenceStore store = new PreferenceStore();
		boolean first = true;
		String un = "<unchanged>";
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			MagicCardPhisical card = (MagicCardPhisical) iterator.next();
			String count = card.getCount() + "";
			String comment = card.getComment();
			if (comment == null)
				comment = "";
			String ownshership = card.isOwn() ? "Own" : "Not Own";
			String price = card.getPrice() + "";
			if (first) {
				store.setDefault(EditCardsPropertiesDialog.COUNT_FIELD, count);
				store.setDefault(EditCardsPropertiesDialog.COMMENT_FIELD, comment);
				store.setDefault(EditCardsPropertiesDialog.OWNERSHIP_FIELD, ownshership);
				store.setDefault(EditCardsPropertiesDialog.NAME_FIELD, card.getName());
				store.setDefault(EditCardsPropertiesDialog.PRICE_FIELD, price);
				first = false;
			} else {
				if (!count.equals(store.getDefaultString(EditCardsPropertiesDialog.COUNT_FIELD))) {
					store.setDefault(EditCardsPropertiesDialog.COUNT_FIELD, un);
				}
				if (!ownshership.equals(store.getDefaultString(EditCardsPropertiesDialog.OWNERSHIP_FIELD))) {
					store.setDefault(EditCardsPropertiesDialog.OWNERSHIP_FIELD, un);
				}
				if (!comment.equals(store.getDefaultString(EditCardsPropertiesDialog.COMMENT_FIELD))) {
					store.setDefault(EditCardsPropertiesDialog.COMMENT_FIELD, un);
				}
				if (!price.equals(store.getDefaultString(EditCardsPropertiesDialog.PRICE_FIELD))) {
					store.setDefault(EditCardsPropertiesDialog.PRICE_FIELD, un);
				}
				store.setDefault(EditCardsPropertiesDialog.NAME_FIELD, "<Multiple Cards>");
			}
		}
		new EditCardsPropertiesDialog(manager.getViewer().getControl().getShell(), store).open();
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			MagicCardPhisical card = (MagicCardPhisical) iterator.next();
			editCard(card, store);
		}
	}

	private void editCard(MagicCardPhisical card, PreferenceStore store) {
		String un = "<unchanged>";
		boolean modified = false;
		String count = card.getCount() + "";
		String comment = card.getComment();
		String price = card.getPrice() + "";
		if (comment == null)
			comment = "";
		String ownshership = card.isOwn() ? "Own" : "Not Own";
		String ecount = store.getString(EditCardsPropertiesDialog.COUNT_FIELD);
		if (!un.equals(ecount) && !ecount.equals(count)) {
			try {
				int x = Integer.parseInt(ecount);
				card.setCount(x);
				modified = true;
			} catch (NumberFormatException e) {
				// was bad value
				MessageDialog.openError(getShell(), "Error", "Invalid value for count: " + ecount);
			}
		}
		String eprice = store.getString(EditCardsPropertiesDialog.PRICE_FIELD);
		if (!un.equals(eprice) && !eprice.equals(price)) {
			try {
				float x = Float.parseFloat(eprice);
				card.setPrice(x);
				modified = true;
			} catch (NumberFormatException e) {
				// was bad value
				MessageDialog.openError(getShell(), "Error", "Invalid value for price: " + eprice);
			}
		}
		String ecomment = store.getString(EditCardsPropertiesDialog.COMMENT_FIELD);
		if (!un.equals(ecomment) && !ecomment.equals(comment)) {
			if (ecomment.trim().length() == 0) {
				if (card.getComment() != null) {
					card.setComment(null);
					modified = true;
				}
			} else {
				card.setComment(ecomment);
				modified = true;
			}
		}
		String eown = store.getString(EditCardsPropertiesDialog.OWNERSHIP_FIELD);
		if (!un.equals(eown) && !eown.equals(ownshership)) {
			if (eown.equals("Own")) {
				card.setOwn(true);
				modified = true;
			}
			if (eown.equals("Not Own")) {
				card.setOwn(false);
				modified = true;
			}
		}
		if (modified) {
			this.manager.getFilteredStore().getCardStore().update(card);
		}
	}
}
