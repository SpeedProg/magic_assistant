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

import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.storage.CollectionCardStore;

/**
 * @author Alena
 *
 */
public class CollectionMultiFileCardStore extends CollectionCardStore implements ICardCountable {
	public CollectionMultiFileCardStore() {
		super(new MultiFileCardStore());
	}

	/**
	 * @param file
	 * @param location
	 */
	public void addFile(File file, String location) {
		((MultiFileCardStore) this.storage).addFile(file, location);
	}

	/**
	 * @param location
	 */
	public void setDefault(String location) {
		((MultiFileCardStore) this.storage).setDefault(location);
	}
}
