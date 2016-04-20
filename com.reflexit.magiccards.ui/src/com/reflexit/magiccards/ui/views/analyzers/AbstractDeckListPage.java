package com.reflexit.magiccards.ui.views.analyzers;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.Presentation;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

public abstract class AbstractDeckListPage extends AbstractMagicCardsListControl implements IDeckPage {
	public AbstractDeckListPage() {
		super(Presentation.TREE);
	}

	public ICardStore<IMagicCard> getCardStore() {
		if (getViewPart() != null && getDeckView().getCardCollection() != null)
			return getDeckView().getCardCollection().getStore();
		return null;
	}

	@Override
	protected IFilteredCardStore<ICard> doGetFilteredStore() {
		return new MemoryFilteredCardStore<>();
	}

	public DeckView getDeckView() {
		return (DeckView) getViewPart();
	}

	@Override
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

	@Override
	public void handleEvent(CardEvent event) {
		mcpEventHandler(event);
	}
}
