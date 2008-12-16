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
import com.reflexit.magiccards.core.model.storage.ILocatable;

/**
 * @author Alena
 * 
 */
public class CollectionMultiFileCardStore extends CollectionCardStore implements ICardCountable, ILocatable {
	public CollectionMultiFileCardStore() {
		super(new MultiFileCardStore());
	}

	/**
	 * @param file
	 * @param location
	 */
	public void addFile(final File file, final String location) {
		addFile(file, location, true);
	}

	public void addFile(final File file, final String location, boolean reload) {
		getStorage().addFile(file, location);
		if (reload)
			setInitialized(false);
	}

	private MultiFileCardStore getStorage() {
		return ((MultiFileCardStore) this.storage);
	}

	/**
	 * @param location
	 */
	public void setLocation(final String location) {
		getStorage().setLocation(location);
	}

	public String getLocation() {
		return getStorage().getLocation();
	}

	public void removeFile(String location) {
		getStorage().removeFile(location);
	}

	@Override
	public void clear() {
		getStorage().clear();
		super.clear();
	}

	public void renameLocation(String oldLocation, String newLocation) {
		getStorage().renameLocation(oldLocation, newLocation);
	}
}
