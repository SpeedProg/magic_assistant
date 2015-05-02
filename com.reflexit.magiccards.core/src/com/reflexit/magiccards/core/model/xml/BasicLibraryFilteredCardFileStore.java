package com.reflexit.magiccards.core.model.xml;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class BasicLibraryFilteredCardFileStore extends AbstractFilteredCardStore<IMagicCard> {
	protected CollectionMultiFileCardStore table;

	@Override
	public ICardStore<IMagicCard> getCardStore() {
		initialize();
		return this.table;
	}

	public BasicLibraryFilteredCardFileStore(CollectionMultiFileCardStore store) {
		table = store;
	}

	@Override
	protected void doInitialize() throws MagicException {
		super.doInitialize();
		if (table == null)
			throw new IllegalStateException();
	}

	@Override
	public void reload() {
		this.table.setInitialized(false);
		super.reload();
		update();
	}

	public ICardStore<IMagicCard> getStore(Location location) {
		initialize();
		if (location == null)
			return table.getStore(table.getLocation());
		return table.getStore(location);
	}
}
