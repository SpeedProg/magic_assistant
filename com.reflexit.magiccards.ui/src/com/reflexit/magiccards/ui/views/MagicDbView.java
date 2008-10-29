package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PartInitException;

import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardDeck;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.MagicDbViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.card.CardDescView;
import com.reflexit.magiccards.ui.views.lib.DeckView;

public class MagicDbView extends AbstractCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.MagicDbView";
	Action addToLib;
	MenuManager addToDeck;

	/**
	 * The constructor.
	 */
	public MagicDbView() {
	}

	@Override
	public ViewerManager doGetViewerManager(AbstractCardsView abstractCardsView) {
		return new LazyTableViewerManager(this);
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		return DataManager.getCardHandler().getMagicCardHandler();
	}

	@Override
	protected void runDoubleClick() {
		try {
			getViewSite().getWorkbenchWindow().getActivePage().showView(CardDescView.ID);
		} catch (PartInitException e) {
			MagicUIActivator.log(e);
		}
	}

	protected void addToLibrary() {
		ISelection selection = getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (!sel.isEmpty()) {
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof IMagicCard)
						DataManager.getCardHandler().getMagicLibraryHandler().getCardStore().addCard(o);
				}
			}
		}
	}

	/**
	 * @param secondaryId
	 */
	protected void addToDeck(String id) {
		ISelection selection = getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (!sel.isEmpty()) {
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof IMagicCard)
						DataManager.getCardHandler().getDeckHandler(id).getCardStore().addCard(o);
				}
			}
		}
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.addToLib = new Action("Add to Library") {
			@Override
			public void run() {
				addToLibrary();
			}
		};
		this.addToDeck = new MenuManager("Add to Deck");
		this.addToDeck.setRemoveAllWhenShown(true);
		this.addToDeck.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillDeckMenu(manager);
			}
		});
	}

	/**
	 * @param manager
	 */
	protected void fillDeckMenu(IMenuManager manager) {
		boolean any = false;
		IViewReference[] views = getViewSite().getWorkbenchWindow().getActivePage().getViewReferences();
		for (int i = 0; i < views.length; i++) {
			final IViewReference viewReference = views[i];
			if (viewReference.getId().equals(DeckView.ID)) {
				final String deckId = viewReference.getSecondaryId();
				ICardDeck store = (ICardDeck) ((DeckView) viewReference.getPart(false)).getFilteredStore()
				        .getCardStore();
				Action ac = new Action(store.getDeckName()) {
					@Override
					public void run() {
						addToDeck(deckId);
					}
				};
				manager.add(ac);
				any = true;
			}
		}
		if (!any) {
			Action ac = new Action("No Open Decks") {
			};
			manager.add(ac);
			ac.setEnabled(false);
		}
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		manager.add(this.addToLib);
		manager.add(this.addToDeck);
		super.fillContextMenu(manager);
	}

	@Override
	protected String getPreferencePageId() {
		return MagicDbViewPreferencePage.class.getName();
	}

	@Override
	protected String getPrefenceColumnsId() {
		return PreferenceConstants.MDBVIEW_COLS;
	}
}