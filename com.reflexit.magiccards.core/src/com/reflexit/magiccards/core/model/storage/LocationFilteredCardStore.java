package com.reflexit.magiccards.core.model.storage;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;

public class LocationFilteredCardStore extends AbstractFilteredCardStore<IMagicCard> {
	private ICardStore<IMagicCard> table;

	@Override
	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	@Override
	protected void doInitialize() throws MagicException {
		this.table.initialize();
		getFilteredList();
	}

	public LocationFilteredCardStore() {
	}

	public LocationFilteredCardStore(Location location) {
		setLocation(location);
	}

	@Override
	public Location getLocation() {
		return table.getLocation();
	}

	@Override
	public void setLocation(Location location) {
		IFilteredCardStore lib = DataManager.getCardHandler().getLibraryFilteredStore();
		if (lib.getCardStore() instanceof AbstractMultiStore) {
			this.table = ((AbstractMultiStore) lib.getCardStore()).getStore(location);
		}
		if (table == null) {
			throw new NullPointerException();
		}
		initialize();
	}
}
