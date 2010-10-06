package com.reflexit.magiccards.core.xml;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class BasicMagicDBXmlFilteredCardStore extends AbstractFilteredCardStore<IMagicCard> {
	protected VirtualMultiFileCardStore table;

	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	protected BasicMagicDBXmlFilteredCardStore(VirtualMultiFileCardStore store) {
		this.table = store;
	}

	@Override
	protected void doInitialize() throws MagicException {
	}
}
