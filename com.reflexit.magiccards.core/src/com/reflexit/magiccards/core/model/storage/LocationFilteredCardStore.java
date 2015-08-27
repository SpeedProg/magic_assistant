package com.reflexit.magiccards.core.model.storage;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;

public class LocationFilteredCardStore extends AbstractFilteredCardStore<IMagicCard> {
	@Override
	protected void doInitialize() throws MagicException {
		super.doInitialize();
		this.store.initialize();
	}

	public LocationFilteredCardStore() {
		super(null);
	}

	public LocationFilteredCardStore(Location location) {
		super(null);
		setLocation(location);
	}

	@Override
	public Location getLocation() {
		if (store == null) {
			return Location.NO_WHERE;
		}
		return store.getLocation();
	}

	@Override
	public void setLocation(Location location) {
		IFilteredCardStore lib = DataManager.getCardHandler().getLibraryFilteredStore();
		if (lib.getCardStore() instanceof AbstractMultiStore) {
			this.store = ((AbstractMultiStore) lib.getCardStore()).getStore(location);
		}
		if (store == null) {
			throw new NullPointerException();
		}
		initialize();
	}
}
