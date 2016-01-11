package com.reflexit.magiccards.ui.views.analyzers;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

public abstract class AbstractDeckListPage extends AbstractMagicControlViewPage implements IDeckPage {
	protected ICardStore store;

	@Override
	public void setFilteredStore(IFilteredCardStore store) {
		this.store = store.getCardStore();
	}

	public ICardStore<IMagicCard> getCardStore() {
		if (store == null && getViewPart() != null && getDeckView().getCardCollection() != null)
			store = getDeckView().getCardCollection().getStore();
		return store;
	}

	public DeckView getDeckView() {
		return (DeckView) getViewPart();
	}

	protected int getCount(Object element) {
		if (element == null)
			return 0;
		int count = ((element instanceof ICardCountable) ? ((ICardCountable) element).getCount() : 1);
		return count;
	}

	protected IStorageInfo getStorageInfo() {
		IStorage storage = getCardStore().getStorage();
		if (storage instanceof IStorageInfo) {
			IStorageInfo si = ((IStorageInfo) storage);
			return si;
		}
		return null;
	}
}
