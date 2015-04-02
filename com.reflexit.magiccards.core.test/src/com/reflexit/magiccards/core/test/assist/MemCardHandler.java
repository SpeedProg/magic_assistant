package com.reflexit.magiccards.core.test.assist;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;

public class MemCardHandler extends AbstractFilteredCardStore<IMagicCard> {
	private MemoryCardStore table;

	@Override
	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	public MemCardHandler() {
		this.table = new MemoryCardStore();
	}

	@Override
	public Location getLocation() {
		return Location.valueOf("mem");
	}
}
