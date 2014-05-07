package com.reflexit.magiccards.core.xml;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class MagicDBFilteredCardFileStore extends BasicMagicDBFilteredCardFileStore {
	private static MagicDBFilteredCardFileStore instance;

	private MagicDBFilteredCardFileStore() {
		super(new DbMultiFileCardStore(true));
	}

	public static synchronized IFilteredCardStore getInstance() {
		if (instance == null)
			instance = new MagicDBFilteredCardFileStore();
		return instance;
	}
}
