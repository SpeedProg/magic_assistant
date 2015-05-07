package com.reflexit.magiccards.core.model.xml;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.LocationPath;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class DeckFilteredCardFileStore extends AbstractFilteredCardStore<IMagicCard> {
	private String filename;

	public DeckFilteredCardFileStore(String filename) {
		super(null);
		this.filename = filename;
	}

	@Override
	protected void doInitialize() throws MagicException {
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
			throw new MagicException("Cannot open file " + filename);
		}
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
