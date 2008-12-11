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
package com.reflexit.magiccards.core.test;

import java.io.File;

import com.reflexit.magiccards.core.test.assist.CardGenerator;
import com.reflexit.magiccards.core.xml.CollectionMultiFileCardStore;

/**
 * @author Alena
 * 
 */
public class CollectionStoreTest extends DeckStoreTest {
	@Override
	protected void setUp() throws Exception {
		String name = "aaa";
		File tempFile = File.createTempFile("coll", ".xml");
		tempFile.deleteOnExit();
		this.store = new CollectionMultiFileCardStore();
		((CollectionMultiFileCardStore) this.store).addFile(tempFile, name);
		((CollectionMultiFileCardStore) this.store).setLocation(name);
		this.m1 = CardGenerator.generateRandomCard();
		this.m1.setName("name 1");
		this.m2 = CardGenerator.generateRandomCard();
		this.m2.setName("name 2");
	}

	@Override
	public void testName() {
	}
}
