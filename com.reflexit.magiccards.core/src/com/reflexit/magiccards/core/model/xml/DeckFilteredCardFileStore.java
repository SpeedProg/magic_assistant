package com.reflexit.magiccards.core.model.xml;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class DeckFilteredCardFileStore extends AbstractFilteredCardStore<IMagicCard> {
	private ICardStore<IMagicCard> table;

	@Override
	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	@Override
	protected void doInitialize() throws MagicException {
		this.table.initialize();
	}

	public DeckFilteredCardFileStore(String filename) {
		CardCollection d = getModelRoot().findCardCollectionById(filename);
		if (d == null)
			d = getModelRoot().findCardCollectionById(filename + ".xml");
		if (d == null)
			throw new IllegalArgumentException("Not found: " + filename);
		if (!d.isOpen()) {
			LibraryFilteredCardFileStore magicLibraryHandler = (LibraryFilteredCardFileStore) DataManager.getCardHandler()
					.getLibraryFilteredStore();
			ICardStore<IMagicCard> store = magicLibraryHandler.getStore(d.getLocation());
			d.open(store);
		}
		this.table = d.getStore();
		if (table == null) {
			throw new NullPointerException(filename);
		}
	}

	public ModelRoot getModelRoot() {
		return DataManager.getInstance().getModelRoot();
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
