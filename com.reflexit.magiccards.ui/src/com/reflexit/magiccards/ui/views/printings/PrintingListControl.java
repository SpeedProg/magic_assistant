package com.reflexit.magiccards.ui.views.printings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
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
	public String getStatusMessage() {
		if (card == MagicCard.DEFAULT || card == null) {
			return "No card";
		}
		return card.getName() + ": " + getStatusMessage1();
	}

	@Override
	protected void sort(int index) {
		updateSortColumn(index);
		updateViewer();
	}

	public String getStatusMessage1() {
		IFilteredCardStore filteredStore = getFilteredStore();
		if (filteredStore == null)
			return "";
		ICardStore cardStore = filteredStore.getCardStore();
		int totalSize = cardStore.size();
		if (totalSize == 1)
			return "Only one version found";
		return "Total " + totalSize + " diffrent versions";
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
		mstore.addAll(searchInStore(DataManager.getCardHandler().getMagicDBStore()));
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
