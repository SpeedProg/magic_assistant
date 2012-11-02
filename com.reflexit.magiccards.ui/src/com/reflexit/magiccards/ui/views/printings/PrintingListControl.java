package com.reflexit.magiccards.ui.views.printings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.Languages.Language;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;

public class PrintingListControl extends AbstractMagicCardsListControl {
	private IMagicCard card;

	public PrintingListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
	}

	@Override
	public IMagicColumnViewer createViewerManager() {
		return new PrintingsManager(getPreferencePageId());
	}

	@Override
	protected MenuManager createGroupMenu() {
		MenuManager groupMenu = new MenuManager("Group By");
		groupMenu.add(createGroupActionNone());
		groupMenu.add(createGroupAction(MagicCardField.SET));
		groupMenu.add(createGroupAction(MagicCardFieldPhysical.LOCATION));
		groupMenu.add(createGroupAction(MagicCardFieldPhysical.OWNERSHIP));
		return groupMenu;
	}

	@Override
	protected void updateStatus() {
		if (card != MagicCard.DEFAULT && card != null)
			setStatus(card.getName() + ": " + getStatusMessage());
		else
			super.updateStatus();
	}

	@Override
	protected void sort(int index) {
		updateSortColumn(index);
		updateViewer();
	}

	@Override
	public String getStatusMessage() {
		IFilteredCardStore filteredStore = getFilteredStore();
		if (filteredStore == null)
			return "";
		ICardStore cardStore = filteredStore.getCardStore();
		int totalSize = cardStore.size();
		int count = totalSize;
		if (cardStore instanceof ICardCountable) {
			count = ((ICardCountable) cardStore).getCount();
		}
		if (isDbMode()) {
			if (totalSize == 1)
				return "Only one version found";
			return "Total " + totalSize + " diffrent versions";
		} else {
			String s = "";
			if (count != 1)
				s = "s";
			return "Total " + count + " card" + s + " in your collections";
		}
	}

	boolean isDbMode() {
		return ((PrintingsManager) manager).isDbMode();
	}

	@Override
	public void updateGroupBy(ICardField[] field) {
		if (((PrintingsManager) manager).isDbMode())
			return;
		super.updateGroupBy(field);
	}

	public void updateDbMode(boolean mode) {
		((PrintingsManager) manager).updateDbMode(mode);
		if (mode)
			updateGroupBy(null);
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
		if (isDbMode()) {
			mstore.addAll(searchInStore(DataManager.getCardHandler().getMagicDBStore()));
		} else {
			mstore.addAll(searchInStore(DataManager.getCardHandler().getLibraryCardStore()));
		}
		monitor.done();
	}

	public Collection<IMagicCard> searchInStore(ICardStore<IMagicCard> store) {
		ArrayList<IMagicCard> res = new ArrayList<IMagicCard>();
		if (card == null || card == MagicCard.DEFAULT)
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
			ArrayList<IMagicCard> res2 = new ArrayList<IMagicCard>();
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
}
