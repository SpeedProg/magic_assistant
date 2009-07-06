/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.xml.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import com.reflexit.magiccards.core.DataManager;
import com.thoughtworks.xstream.XStream;

/**
 * Object that holds xml. Fields should not be renamed as well as class anme itself.
 *
 */
public class CardCollectionStoreObject {
	public String name;
	public String key;
	public String comment;
	public String cardCount;
	public String type;
	public Properties properties = new Properties();
	public List list;
	public transient File file;
	public transient static XStream xstream;
	static {
		xstream = DataManager.getXStream();
		xstream.alias("com.reflexit.magiccards.core.xml.LibraryCardStore", CardCollectionStoreObject.class);
		xstream.alias("com.reflexit.magiccards.core.xml.DeckFileCardStore", CardCollectionStoreObject.class);
		xstream.alias("cards", CardCollectionStoreObject.class);
		xstream.setClassLoader(CardCollectionStoreObject.class.getClassLoader());
	}

	public CardCollectionStoreObject() {
		// private constructor
	}

	public static CardCollectionStoreObject initFromFile(File file) throws IOException {
		if (file.exists() && file.length() > 0) {
			FileInputStream is = new FileInputStream(file);
			Charset encoding = Charset.forName("utf-8");
			Object object = xstream.fromXML(new InputStreamReader(is, encoding));
			is.close();
			CardCollectionStoreObject store = convertToStore(object);
			store.file = file;
			return store;
		} else {
			// create empty file
			CardCollectionStoreObject store = new CardCollectionStoreObject();
			store.file = file;
			store.save();
			return store;
		}
	}

	public void save() throws FileNotFoundException {
		OutputStream out = new FileOutputStream(this.file);
		xstream.toXML(this, new OutputStreamWriter(out, Charset.forName("utf-8")));
		try {
			out.close();
		} catch (IOException e) {
			// ignore
		}
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
}
