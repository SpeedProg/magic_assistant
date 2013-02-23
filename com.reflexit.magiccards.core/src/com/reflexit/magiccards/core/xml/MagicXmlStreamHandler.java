package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MagicXmlStreamHandler implements IStoreHandler {
	@Override
	public CardCollectionStoreObject load(File file) throws IOException {
		return new MagicXmlStreamReader().load(file);
	}

	public CardCollectionStoreObject load(InputStream st) throws IOException {
		return new MagicXmlStreamReader().load(st);
	}

	@Override
	public void save(CardCollectionStoreObject object) throws IOException {
		new MagicXmlStreamWriter().write(object);
	}

	public void save(CardCollectionStoreObject object, OutputStream st) throws IOException {
		new MagicXmlStreamWriter().write(object, st);
	}
}
