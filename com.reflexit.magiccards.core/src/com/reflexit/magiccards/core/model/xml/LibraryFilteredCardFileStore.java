package com.reflexit.magiccards.core.model.xml;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;

public class LibraryFilteredCardFileStore extends BasicLibraryFilteredCardFileStore {
	private static LibraryFilteredCardFileStore instance;

	@Override
	protected void doInitialize() throws MagicException {
		MagicLogger.traceStart("libinit");
		super.doInitialize();
		CollectionMultiFileCardStore table = getMultiStore();
		table.initialize();
		initialized = true;
		MagicLogger.traceEnd("libinit");
	}

	public synchronized static LibraryFilteredCardFileStore getInstance() {
		if (instance == null)
			instance = new LibraryFilteredCardFileStore();
		return instance;
	}

	private LibraryFilteredCardFileStore() {
		super(LibraryCardStore.getInstance());
	}
}
