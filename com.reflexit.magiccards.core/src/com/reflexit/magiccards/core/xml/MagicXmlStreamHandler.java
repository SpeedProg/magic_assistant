package com.reflexit.magiccards.core.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.IMagicCard;

public class MagicXmlStreamHandler implements IStoreHandler {
	private static final MagicXmlStreamReader reader = new MagicXmlStreamReader();
	private static final MagicXmlStreamWriter writer = new MagicXmlStreamWriter();

	@Override
	public CardCollectionStoreObject load(File file) throws IOException {
		return reader.load(file);
	}

	public CardCollectionStoreObject load(InputStream st) throws IOException {
		return reader.load(st);
	}

	@Override
	public void save(CardCollectionStoreObject object) throws IOException {
		writer.write(object);
	}

	public void save(CardCollectionStoreObject object, OutputStream st) throws IOException {
		writer.write(object, st);
	}

	public String toXML(IMagicCard card) {
		CardCollectionStoreObject object = new CardCollectionStoreObject();
		object.list = new ArrayList<IMagicCard>();
		object.list.add(card);
		ByteArrayOutputStream st = new ByteArrayOutputStream();
		try {
			save(object, st);
		} catch (IOException e) {
			// ignore
		}
		try {
			st.close();
		} catch (IOException e) {
			// ignore
		}
		return st.toString();
	}

	public CardCollectionStoreObject fromXML(String xml) {
		try {
			return load(new ByteArrayInputStream(xml.getBytes(FileUtils.CHARSET_UTF_8)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
