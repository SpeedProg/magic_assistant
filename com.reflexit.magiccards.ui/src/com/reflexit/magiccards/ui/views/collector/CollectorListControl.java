package com.reflexit.magiccards.ui.views.collector;

import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.MenuManager;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dialogs.MyCardsFilterDialog;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;

public class CollectorListControl extends AbstractMagicCardsListControl {
	private static final ICardField[] DEF_GROUP = new ICardField[] { MagicCardField.SET, MagicCardField.LANG, MagicCardField.RARITY };

	public CollectorListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
	}

	@Override
	public IMagicColumnViewer createViewerManager() {
		return new CollectorManager(getPreferencePageId());
	}

	@Override
	protected MenuManager createGroupMenu() {
		MenuManager groupMenu = new MenuManager("Group By");
		groupMenu.add(createGroupAction("Set/Lang/Rarity", DEF_GROUP));
		groupMenu.add(createGroupAction("Set/Rarity", new ICardField[] { MagicCardField.SET, MagicCardField.RARITY }));
		groupMenu.add(createGroupAction(MagicCardField.SET));
		groupMenu.add(createGroupAction("Core/Block/Set/Lang/Rarity", new ICardField[] { MagicCardField.SET_CORE, MagicCardField.SET_BLOCK,
				MagicCardField.SET, MagicCardField.LANG, MagicCardField.RARITY }));
		// groupMenu.add(createGroupAction(MagicCardField.ARTIST));
		return groupMenu;
	}

	@Override
	protected void runShowFilter() {
		MyCardsFilterDialog cardFilterDialog = new MyCardsFilterDialog(getShell(), getFilterPreferenceStore());
		if (cardFilterDialog.open() == IStatus.OK)
			reloadData();
	}

	@Override
	protected void initManager() {
		getLocalPreferenceStore().setDefault(FilterField.GROUP_FIELD.toString(), createGroupName(DEF_GROUP));
		super.initManager();
	}

	@Override
	public void fillContextMenu(org.eclipse.jface.action.IMenuManager manager) {
		manager.add(this.actionShowFind);
		super.fillContextMenu(manager);
	}

	public int getOwnSize(Iterable children) {
		int count = 0;
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof MagicCardPhysical) {
				if (((IMagicCardPhysical) object).isOwn())
					count++;
			} else if (object instanceof MagicCard) {
				if (((MagicCard) object).getOwnCount() > 0)
					count++;
			}
		}
		return count;
	}

	@Override
	public String getStatusMessage() {
		IFilteredCardStore filteredStore = getFilteredStore();
		if (filteredStore == null)
			return "";
		ICardStore cardStore = filteredStore.getCardStore();
		return "Total " + getOwnSize(cardStore) + " unique card(s) in your collections";
	}

	@Override
	public void handleEvent(final CardEvent event) {
		int type = event.getType();
		if (type == CardEvent.UPDATE || type == CardEvent.REMOVE || type == CardEvent.ADD) {
			reloadData();
		}
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		return DataManager.getCardHandler().getMagicDBFilteredStoreWorkingCopy();
	}
	/*
	 * @Override public IFilteredCardStore doGetFilteredStore() {
	 * MemoryFilteredCardStore<IMagicCard> fstore = new MemoryFilteredCardStore<IMagicCard>();
	 * return fstore; }
	 * 
	 * @Override protected void populateStore(IProgressMonitor monitor) {
	 * super.populateStore(monitor); ((MemoryFilteredCardStore<ICard>) fstore).clear(); ICardStore
	 * lib = DataManager.getCardHandler().getLibraryFilteredStore().getCardStore(); // ICardStore
	 * magicDB = DataManager.getCardHandler().getMagicDBStore(); ArrayList<IMagicCard> list = new
	 * ArrayList<IMagicCard>(lib.size()); // for (Iterator iterator = magicDB.iterator();
	 * iterator.hasNext();) { // IMagicCard card = (IMagicCard) iterator.next(); // list.add(card);
	 * // } HashSet<String> editions = new HashSet<String>(); for (Iterator iterator =
	 * lib.iterator(); iterator.hasNext();) { IMagicCard card = (IMagicCard) iterator.next();
	 * list.add(card); editions.add(card.getSet()); } ICardStore cardStore =
	 * getFilteredStore().getCardStore(); cardStore.addAll(list); for (Iterator iterator =
	 * Editions.getInstance().getNames().iterator(); iterator.hasNext();) { String set = (String)
	 * iterator.next(); Location loc = Location.createLocationFromSet(set); ICardStore<IMagicCard>
	 * store = ((AbstractMultiStore<IMagicCard>)
	 * DataManager.getCardHandler().getMagicDBStore()).getStore(loc); if (store == null) continue;
	 * for (Iterator iterator2 = store.iterator(); iterator2.hasNext();) { IMagicCard card =
	 * (IMagicCard) iterator2.next(); if (!cardStore.contains(card)) { cardStore.add(card); } } }
	 * getFilteredStore().update(); }
	 */
}
