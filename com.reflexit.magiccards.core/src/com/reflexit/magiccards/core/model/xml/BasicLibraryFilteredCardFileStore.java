package com.reflexit.magiccards.core.model.xml;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class BasicLibraryFilteredCardFileStore extends AbstractFilteredCardStore<IMagicCard> {
	public BasicLibraryFilteredCardFileStore(CollectionMultiFileCardStore store) {
		super(store);
	}

	protected CollectionMultiFileCardStore getMultiStore() {
		return (CollectionMultiFileCardStore) getCardStore();
	}

	@Override
	public void reload() {
		getMultiStore().setInitialized(false);
		setRefreshRequired(true);
		super.reload();
		update();
	}

	public ICardStore<IMagicCard> getStore(Location location) {
		initialize();
		if (location == null) location = getMultiStore().getLocation();
		return getMultiStore().getStore(location);
	}
}
