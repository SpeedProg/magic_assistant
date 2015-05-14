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
		ICardStore<IMagicCard> store = getStoreForKey(filename);
		if (store == null) {
			throw new MagicException("Cannot open file " + filename);
		}
		this.store = store;
	}

	public static ICardStore<IMagicCard> getStoreForKey(String filename) {
		LibraryFilteredCardFileStore lib = (LibraryFilteredCardFileStore) DataManager
				.getCardHandler()
				.getLibraryFilteredStore();
		Location location = Location.createLocation(filename);
		ICardStore<IMagicCard> store = lib.getStore(location);
		return store;
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
