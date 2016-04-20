package com.reflexit.magiccards.ui.views.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.GroupOrder;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.ExtendedTreeViewer;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.Presentation;
import com.reflexit.magiccards.ui.views.model.RootTreeViewerContentProvider;

public class CollectorListControl extends AbstractMagicCardsListControl {
	public static final ICardField[] DEF_GROUP = new ICardField[] { MagicCardField.SET, MagicCardField.LANG,
			MagicCardField.RARITY };

	public CollectorListControl() {
		super(Presentation.TREE);
	}

	@Override
	public IMagicColumnViewer createViewer(Composite parent) {
		ExtendedTreeViewer v = new ExtendedTreeViewer(parent, new CollectorColumnCollection());
		v.setContentProvider(new RootTreeViewerContentProvider());
		return v;
	}

	@Override
	protected Collection<GroupOrder> getGroups() {
		ArrayList<GroupOrder> res = new ArrayList<>();
		res.add(new GroupOrder(DEF_GROUP));
		res.add(new GroupOrder(MagicCardField.SET, MagicCardField.RARITY));
		res.add(new GroupOrder(MagicCardField.SET));
		res.add(new GroupOrder("Core/Block/Set/Lang/Rarity", MagicCardField.SET_CORE, MagicCardField.SET_BLOCK,
				MagicCardField.SET, MagicCardField.LANG, MagicCardField.RARITY));
		return res;
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
		mcpEventHandler(event);
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		return DataManager.getCardHandler().getMagicDBFilteredStoreWorkingCopy();
	}
	/*
	 * @Override public IFilteredCardStore doGetFilteredStore() {
	 * MemoryFilteredCardStore<IMagicCard> fstore = new
	 * MemoryFilteredCardStore<IMagicCard>(); return fstore; }
	 *
	 * @Override protected void populateStore(IProgressMonitor monitor) {
	 * super.populateStore(monitor); ((MemoryFilteredCardStore<ICard>)
	 * fstore).clear(); ICardStore lib =
	 * DataManager.getCardHandler().getLibraryFilteredStore().getCardStore(); //
	 * ICardStore magicDB = DataManager.getCardHandler().getMagicDBStore();
	 * ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(lib.size()); //
	 * for (Iterator iterator = magicDB.iterator(); iterator.hasNext();) { //
	 * IMagicCard card = (IMagicCard) iterator.next(); // list.add(card); // }
	 * HashSet<String> editions = new HashSet<String>(); for (Iterator iterator
	 * = lib.iterator(); iterator.hasNext();) { IMagicCard card = (IMagicCard)
	 * iterator.next(); list.add(card); editions.add(card.getSet()); }
	 * ICardStore cardStore = getFilteredStore().getCardStore();
	 * cardStore.addAll(list); for (Iterator iterator =
	 * Editions.getInstance().getNames().iterator(); iterator.hasNext();) {
	 * String set = (String) iterator.next(); Location loc =
	 * Location.createLocationFromSet(set); ICardStore<IMagicCard> store =
	 * ((AbstractMultiStore<IMagicCard>)
	 * DataManager.getCardHandler().getMagicDBStore()).getStore(loc); if (store
	 * == null) continue; for (Iterator iterator2 = store.iterator();
	 * iterator2.hasNext();) { IMagicCard card = (IMagicCard) iterator2.next();
	 * if (!cardStore.contains(card)) { cardStore.add(card); } } }
	 * getFilteredStore().update(); }
	 */

	@Override
	protected String getPreferencePageId() {
		return getViewPreferencePageId();
	}
}
