package com.reflexit.magiccards.core.test.assist;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;

public class MemCardHandler extends AbstractFilteredCardStore<IMagicCard> {
	private MemoryCardStore table;

	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	@Override
	protected void doInitialize() throws MagicException {
		this.table.initialize();
	}

	public MemCardHandler() {
		this.table = new MemoryCardStore();
	}
}
