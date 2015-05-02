package com.reflexit.magiccards.core.model.xml;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;

public class BasicMagicDBFilteredCardFileStore extends AbstractFilteredCardStore<IMagicCard> {
	protected BasicMagicDBFilteredCardFileStore(DbMultiFileCardStore store) {
		super(store);
	}
}
