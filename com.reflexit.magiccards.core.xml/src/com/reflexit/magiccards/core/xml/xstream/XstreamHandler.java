package com.reflexit.magiccards.core.xml.xstream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.xml.CardCollectionStoreObject;
import com.reflexit.magiccards.core.xml.IStoreHandler;
import com.thoughtworks.xstream.XStream;

public class XstreamHandler implements IStoreHandler {
	public transient static XStream xstream;
	static {
		xstream = getXStream();
		xstream.alias("com.reflexit.magiccards.core.xml.LibraryCardStore", CardCollectionStoreObject.class);
		xstream.alias("com.reflexit.magiccards.core.xml.DeckFileCardStore", CardCollectionStoreObject.class);
		xstream.alias("cards", CardCollectionStoreObject.class);
		xstream.setClassLoader(CardCollectionStoreObject.class.getClassLoader());
		xstream.registerConverter(new MagicCardPhysicalConvertor(xstream.getMapper(), xstream.getReflectionProvider()));
		xstream.registerConverter(new MagicCardConvertor(xstream.getMapper(), xstream.getReflectionProvider()));
	}

	public static XStream getXStream() {
		XStream xstream = new XStream();
		xstream.alias("mc", MagicCard.class);
		xstream.alias("mcp", MagicCardPhysical.class);
		xstream.alias("pfield", MagicCardFieldPhysical.class);
		return xstream;
	}

	@Override
	public CardCollectionStoreObject load(File file) throws IOException {
		FileInputStream is = new FileInputStream(file);
		Charset encoding = Charset.forName("utf-8");
		Object object = xstream.fromXML(new InputStreamReader(is, encoding));
		is.close();
		CardCollectionStoreObject store = convertToStore(object);
		store.file = file;
		return store;
	}

	/**
	 * @param object
	 * @return
	 */
	private static CardCollectionStoreObject convertToStore(Object object) {
		if (object instanceof CardCollectionStoreObject)
			return (CardCollectionStoreObject) object;
		return null;
	}

	@Override
	public void save(CardCollectionStoreObject object) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(object.file), 256 * 1024);
		xstream.toXML(object, new OutputStreamWriter(out, Charset.forName("utf-8")));
		try {
			out.close();
		} catch (IOException e) {
			// ignore
		}
	}
}
