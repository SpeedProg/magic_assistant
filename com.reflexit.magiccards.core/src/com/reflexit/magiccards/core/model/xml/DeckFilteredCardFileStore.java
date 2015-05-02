package com.reflexit.magiccards.core.model.xml;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.LocationPath;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class DeckFilteredCardFileStore extends AbstractFilteredCardStore<IMagicCard> {
	public DeckFilteredCardFileStore(String filename) {
		super(null);
		CardCollection d = getModelRoot().findCardCollectionById(new LocationPath(filename).getId());
		if (d == null) {
			throw new IllegalArgumentException("Not found: " + filename);
		}
		if (!d.isOpen()) {
			LibraryFilteredCardFileStore magicLibraryHandler = (LibraryFilteredCardFileStore) DataManager
					.getCardHandler()
					.getLibraryFilteredStore();
			ICardStore<IMagicCard> store = magicLibraryHandler.getStore(d.getLocation());
			d.open(store);
		}
		this.store = d.getStore();
		if (store == null) {
			throw new NullPointerException(filename);
		}
	}

	@Override
	public Location getLocation() {
		return store.getLocation();
	}

	@Override
	public void setLocation(Location location) {
		store.setLocation(location);
	}
}
