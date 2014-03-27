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

import gnu.trove.map.TIntFloatMap;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.seller.IPriceProvider;

/**
 * Object that holds xml for prices.
 * 
 */
public class PriceProviderStoreObject {
	public String name; // provider name
	public Properties properties = new Properties();
	public TIntFloatMap map;
	public transient File file;
	public String comment;
	public static transient PricesXmlStreamWriter writer = new PricesXmlStreamWriter();
	public static transient PricesXmlStreamReader reader = new PricesXmlStreamReader();

	public PriceProviderStoreObject() {
		// empty
	}

	public PriceProviderStoreObject(IPriceProvider provider) {
		this.name = provider.getName();
		this.map = DataManager.getDBPriceStore().getPriceMap(provider);
	}

	public static PriceProviderStoreObject initFromFile(File file) throws IOException {
		// long time = System.currentTimeMillis();
		try {
			if (file.exists() && file.length() > 0) {
				return reader.load(file);
			} else {
				// create empty file
				PriceProviderStoreObject store = new PriceProviderStoreObject();
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
			new PricesXmlStreamWriter().write(this);
		} finally {
			// System.err.println("save " + file.getName() + "  took " + (System.currentTimeMillis()
			// - time) + "  ms");
		}
	}
}
