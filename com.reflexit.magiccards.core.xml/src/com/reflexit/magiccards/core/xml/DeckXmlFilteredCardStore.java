package com.reflexit.magiccards.core.xml;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class DeckXmlFilteredCardStore extends AbstractFilteredCardStore<IMagicCard> {
	private CollectionSingleFileCardStore table;

	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	@Override
	protected void doInitialize() throws MagicException {
		this.table.initialize();
	}

	public DeckXmlFilteredCardStore(String filename) {
		CardCollection d = DataManager.getModelRoot().findCardCollectionById(filename);
		if (d == null)
			d = DataManager.getModelRoot().findCardCollectionById(filename + ".xml");
		if (d == null)
			throw new IllegalArgumentException("Not found: " + filename);
		if (!d.isOpen()) {
			LibraryXmlFilteredCardStore magicLibraryHandler = (LibraryXmlFilteredCardStore) DataManager.getCardHandler()
					.getLibraryFilteredStore();
			ICardStore<IMagicCard> store = magicLibraryHandler.getStore(d.getLocation());
			d.open(store);
		}
		this.table = (CollectionSingleFileCardStore) d.getStore();
		if (table == null) {
			throw new NullPointerException();
		}
	}

	@Override
	public Location getLocation() {
		return table.getLocation();
	}

	@Override
	public void setLocation(Location location) {
		table.setLocation(location);
	}
}
