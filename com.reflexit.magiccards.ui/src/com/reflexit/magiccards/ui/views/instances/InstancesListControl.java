package com.reflexit.magiccards.ui.views.instances;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.GroupOrder;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Languages.Language;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;

public class InstancesListControl extends AbstractMagicCardsListControl {
	public InstancesListControl() {
		super(true);
	}

	private IMagicCard card;

	@Override
	public IMagicColumnViewer createViewer(Composite parent) {
		return new InstancesViewer(getPreferencePageId(), parent);
	}

	@Override
	public void handleEvent(CardEvent event) {
		mcpEventHandler(event);
	}

	@Override
	protected String getPreferencePageId() {
		return getViewPreferencePageId();
	}

	@Override
	protected Collection<GroupOrder> getGroups() {
		ArrayList<GroupOrder> res = new ArrayList<>();
		res.add(new GroupOrder());
		res.add(new GroupOrder(MagicCardField.SET));
		res.add(new GroupOrder(MagicCardField.LOCATION));
		res.add(new GroupOrder(MagicCardField.OWNERSHIP));
		return res;
	}

	@Override
	public String getStatusMessage() {
		if (card == MagicCard.DEFAULT || card == null) {
			return "No card";
		}
		return card.getName() + ": " + getStatusMessage1();
	}

	@Override
	protected void sort(int index, int dir) {
		updateSortColumn(index);
		refreshViewer();
	}

	public String getStatusMessage1() {
		IFilteredCardStore filteredStore = getFilteredStore();
		if (filteredStore == null)
			return "";
		ICardStore cardStore = filteredStore.getCardStore();
		int totalSize = cardStore.size();
		int count = totalSize;
		if (cardStore instanceof ICardCountable) {
			count = ((ICardCountable) cardStore).getCount();
		}
		String s = "";
		if (count != 1)
			s = "s";
		return "Total " + count + " card" + s + " in your collections";
	}

	public void setCard(IMagicCard card) {
		this.card = card;
	}

	@Override
	protected void populateStore(IProgressMonitor monitor) {
		if (card == IMagicCard.DEFAULT || card == null)
			return;
		monitor.beginTask("Loading card printings for " + card.getName(), 100);
		if (fstore == null) {
			fstore = doGetFilteredStore();
		}
		MemoryFilteredCardStore mstore = (MemoryFilteredCardStore) fstore;
		mstore.clear();
		mstore.addAll(searchInStore(DataManager.getCardHandler().getLibraryCardStore()));
		monitor.done();
	}

	public Collection<IMagicCard> searchInStore(ICardStore<IMagicCard> store) {
		LinkedHashSet<IMagicCard> res = new LinkedHashSet<>();
		if (card == null || card == MagicCard.DEFAULT || card.getName() == null)
			return res;
		String englishName = card.getName();
		String language = card.getLanguage();
		if (language != null && !language.equals(Language.ENGLISH.getLang())) {
			int enId = card.getEnglishCardId();
			if (enId != 0) {
				IMagicCard card2 = store.getCard(enId);
				englishName = card2 != null ? card2.getName() : card.getName();
			}
		}
		boolean multilang = false;
		for (Iterator<IMagicCard> iterator = store.iterator(); iterator.hasNext();) {
			IMagicCard next = iterator.next();
			try {
				if (englishName.equals(next.getName())) {
					res.add(next);
				}
				language = next.getLanguage();
				if (language != null && !language.equals(Language.ENGLISH.getLang())) {
					multilang = true;
				}
			} catch (Exception e) {
				MagicUIActivator.log("Bad card: " + next);
				MagicUIActivator.log(e);
			}
		}
		if (multilang) {
			ArrayList<IMagicCard> res2 = new ArrayList<>();
			for (Iterator<IMagicCard> iterator = store.iterator(); iterator.hasNext();) {
				IMagicCard next = iterator.next();
				try {
					int parentId = next.getEnglishCardId();
					if (parentId != 0) {
						for (Iterator<IMagicCard> iterator2 = res.iterator(); iterator2.hasNext();) {
							IMagicCard mc = iterator2.next();
							if (mc.getCardId() == parentId) {
								if (!res.contains(next))
									res2.add(next);
							}
						}
					}
				} catch (Exception e) {
					MagicUIActivator.log("Bad card: " + next);
					MagicUIActivator.log(e);
				}
			}
			res.addAll(res2);
		}
		return res;
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		return new MemoryFilteredCardStore();
	}

	@Override
	public void saveColumnLayout() {
		// ignore?
	}
}
