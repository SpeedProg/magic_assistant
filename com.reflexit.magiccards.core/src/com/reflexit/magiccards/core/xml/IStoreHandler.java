package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.IOException;

public interface IStoreHandler {
	public CardCollectionStoreObject load(File file) throws IOException;

	public void save(CardCollectionStoreObject object) throws IOException;
}
