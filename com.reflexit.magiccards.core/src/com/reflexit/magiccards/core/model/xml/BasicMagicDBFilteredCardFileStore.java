package com.reflexit.magiccards.core.model.xml;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class BasicMagicDBFilteredCardFileStore extends AbstractFilteredCardStore<IMagicCard> {
	protected DbMultiFileCardStore table;

	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	protected BasicMagicDBFilteredCardFileStore(DbMultiFileCardStore store) {
		this.table = store;
	}

	@Override
	protected void doInitialize() throws MagicException {
		table.initialize();
	}
}
