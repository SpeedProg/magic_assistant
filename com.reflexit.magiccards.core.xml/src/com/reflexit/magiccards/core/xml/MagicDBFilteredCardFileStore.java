package com.reflexit.magiccards.core.xml;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class MagicDBFilteredCardFileStore extends BasicMagicDBFilteredCardFileStore {
	private static MagicDBFilteredCardFileStore instance;

	private MagicDBFilteredCardFileStore() {
		super(new DbMultiFileCardStore(true));
		instance = this;
	}

	public static IFilteredCardStore getInstance() {
		if (instance == null)
			new MagicDBFilteredCardFileStore();
		return instance;
	}
}
