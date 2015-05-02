package com.reflexit.magiccards.core.test.assist;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;

public class MemCardHandler extends AbstractFilteredCardStore<IMagicCard> {
	public MemCardHandler() {
		super(new MemoryCardStore<IMagicCard>());
	}

	@Override
	public Location getLocation() {
		return Location.valueOf("mem");
	}
}
