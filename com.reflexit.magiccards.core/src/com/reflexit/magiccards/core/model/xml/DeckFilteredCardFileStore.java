package com.reflexit.magiccards.core.model.xml;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class DeckFilteredCardFileStore extends AbstractFilteredCardStore<IMagicCard> {
	public DeckFilteredCardFileStore(String filename) {
		super(null);
		ICardStore<IMagicCard> store = getStoreForKey(filename);
		if (store == null) {
			throw new MagicException("Cannot open file " + filename);
		}
		this.store = store;
	}

	public static ICardStore<IMagicCard> getStoreForKey(String filename) {
		LibraryFilteredCardFileStore lib = (LibraryFilteredCardFileStore) DataManager.getCardHandler()
				.getLibraryFilteredStore();
		Location location = Location.createLocation(filename);
		ICardStore<IMagicCard> store = lib.getStore(location);
		if (store != null)
			return store;
		// backward compat
		CardCollection coll = DataManager.getInstance().getModelRoot().findCardCollectionById(location.getName());
		if (coll != null) {
			return coll.getStore();
		}
		return null;
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
