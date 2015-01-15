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
package com.reflexit.magiccards.core.model.xml;

import java.io.File;

import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.storage.CollectionCardStore;

/**
 * Single File store with card count and caching
 * 
 * @author Alena
 * 
 */
public class CollectionSingleFileCardStore extends CollectionCardStore implements ICardCountable {
	/**
	 * @param file
	 */
	public CollectionSingleFileCardStore(File file, Location location) {
		this(file, location, false);
	}

	public CollectionSingleFileCardStore(File file, Location location, boolean initialize) {
		super(new SingleFileCardStorage(file, location, initialize));
	}

	public static CollectionCardStore create(File file, Location location, boolean initialize) {
		return new CollectionSingleFileCardStore(file, location, initialize);
	}

	public void setType(String type) {
		getFileStorage().setType(type);
	}

	@Override
	public void setName(String name) {
		getFileStorage().setName(name);
	}

	@Override
	public void setVirtual(boolean value) {
		getFileStorage().setVirtual(value);
	}

	public void setComment(String comment) {
		getFileStorage().setComment(comment);
	}

	protected SingleFileCardStorage getFileStorage() {
		return (SingleFileCardStorage) getStorage();
	}
}
