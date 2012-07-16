package com.reflexit.magiccards.core.xml.data;

import java.io.File;
import java.io.IOException;

public class MagicXmlStreamHandler implements IStoreHandler {
	@Override
	public CardCollectionStoreObject load(File file) throws IOException {
		return new MagicXmlStreamReader().load(file);
	}

	@Override
	public void save(CardCollectionStoreObject object) throws IOException {
		new MagicXmlStreamWriter().write(object);
	}
}
