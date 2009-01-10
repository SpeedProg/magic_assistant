package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewReference;

import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardDeck;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog2;
import com.reflexit.magiccards.ui.preferences.LibViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.LocationFilterPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;

public class LibView extends MyCardsView implements ICardEventListener {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.LibView";
	private MenuManager addToDeck;

	/**
	 * The constructor.
	 */
	public LibView() {
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.addToDeck = new MenuManager("Add to Deck");
		this.addToDeck.setRemoveAllWhenShown(true);
		this.addToDeck.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillDeckMenu(manager);
			}
		});
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(this.addToDeck);
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		return DataManager.getCardHandler().getMagicLibraryHandler();
	}

	@Override
	protected String getPreferencePageId() {
		return LibViewPreferencePage.class.getName();
	}

	@Override
	protected String getPrefenceColumnsId() {
		return PreferenceConstants.LIBVIEW_COLS;
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
						DataManager.getCardHandler().getDeckHandler(id).getCardStore().add(o);
				}
			}
		}
	}

	/**
	 * @param manager
	 */
	protected void fillDeckMenu(IMenuManager manager) {
		boolean any = false;
		IViewReference[] views = getViewSite().getWorkbenchWindow().getActivePage().getViewReferences();
		for (final IViewReference viewReference : views) {
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
	protected void runShowFilter() {
		// CardFilter.open(getViewSite().getShell());
		CardFilterDialog2 cardFilterDialog = new CardFilterDialog2(getShell(), getPreferenceStore());
		cardFilterDialog.addNode(new PreferenceNode("locations", new LocationFilterPreferencePage(SWT.MULTI)));
		if (cardFilterDialog.open() == IStatus.OK)
			this.manager.loadData(null);
	}

	/**
	 * @param preferenceStore
	 * @param portableString
	 */
	public void setLocationFilter(String loc) {
		IPreferenceStore preferenceStore = getPreferenceStore();
		Collection ids = Locations.getInstance().getIds();
		String locId = Locations.getInstance().getPrefConstant(loc);
		for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			if (id.startsWith(locId)) {
				preferenceStore.setValue(id, true);
			} else {
				preferenceStore.setValue(id, false);
			}
		}
		reloadData();
	}
}