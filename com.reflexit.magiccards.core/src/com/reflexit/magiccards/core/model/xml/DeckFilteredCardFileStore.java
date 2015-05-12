package com.reflexit.magiccards.core.model.xml;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class DeckFilteredCardFileStore extends AbstractFilteredCardStore<IMagicCard> {
	private String filename;

	public DeckFilteredCardFileStore(String filename) {
		super(null);
		this.filename = filename;
		CollectionMultiFileCardStore lib = (CollectionMultiFileCardStore) DataManager
				.getCardHandler()
				.getLibraryCardStore();
		ICardStore<IMagicCard> store = lib.getStore(Location.createLocation(filename));
		this.store = store;
		if (store == null) {
			throw new MagicException("Cannot open file " + filename);
		}
	}

	@Override
	protected void doInitialize() throws MagicException {
		super.doInitialize();
	}

	@Override
	public Location getLocation() {
		initialize();
		return store.getLocation();
	}

	@Override
	public void setLocation(Location location) {
		initialize();
		store.setLocation(location);
	}
}
