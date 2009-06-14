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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog2;
import com.reflexit.magiccards.ui.preferences.LibViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.LocationFilterPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;

public class MyCardsView extends AbstractMyCardsView implements ICardEventListener {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.LibView";
	private MenuManager addToDeck;

	/**
	 * The constructor.
	 */
	public MyCardsView() {
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.addToDeck = new MenuManager("Move to Deck");
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
		return DataManager.getCardHandler().getMyCardsHandler();
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
	protected void addToCardCollection(String id) {
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

	/**
	 * @param manager
	 */
	protected void fillDeckMenu(IMenuManager manager) {
		boolean any = false;
		IViewReference[] views = getViewSite().getWorkbenchWindow().getActivePage().getViewReferences();
		for (final IViewReference viewReference : views) {
			if (viewReference.getId().equals(DeckView.ID)) {
				final String deckId = viewReference.getSecondaryId();
				DeckView deckView = (DeckView) viewReference.getPart(false);
				if (deckView != null) {
					ICardStore<IMagicCard> store = deckView.getFilteredStore().getCardStore();
					Action ac = new Action(store.getName()) {
						@Override
						public void run() {
							addToCardCollection(deckId);
						}
					};
					manager.add(ac);
					any = true;
				}
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