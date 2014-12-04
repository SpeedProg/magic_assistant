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
package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.reflexit.magiccards.core.model.IMagicCard;

/**
 * Object that holds xml. Fields should not be renamed as well as class anme itself.
 * 
 */
public class CardCollectionStoreObject {
	public String name;
	public String key;
	public String comment;
	public String type;
	public Properties properties = new Properties();
	public List<IMagicCard> list;
	public transient File file;
	public static transient IStoreHandler formatHandler = new MagicXmlStreamHandler();

	public CardCollectionStoreObject() {
		// empty
	}

	public CardCollectionStoreObject(List<IMagicCard> cards) {
		this.list = cards;
	}

	public static CardCollectionStoreObject initFromFile(File file) throws IOException {
		// long time = System.currentTimeMillis();
		try {
			if (file.exists() && file.length() > 0) {
				return formatHandler.load(file);
			} else {
				// create empty file
				CardCollectionStoreObject store = new CardCollectionStoreObject();
				store.file = file;
				store.save();
				return store;
			}
		} finally {
			// System.err.println("load " + file.getName() + "  took " + (System.currentTimeMillis()
			// - time) + " ms");
		}
	}

	public void save() throws IOException {
		// long time = System.currentTimeMillis();
		try {
			formatHandler.save(this);
		} finally {
			// System.err.println("save " + file.getName() + "  took " + (System.currentTimeMillis()
			// - time) + "  ms");
		}
	}
}
