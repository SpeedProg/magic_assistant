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
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.CollectionCardStore;

/**
 * Single File store with card count and caching
 * @author Alena
 *
 */
public class DbFileCardStore extends CollectionCardStore implements ICardCountable {
	/**
	 * @param file
	 */
	public DbFileCardStore(File file, Location location) {
		this(file, location, false);
	}

	public DbFileCardStore(File file, Location location, boolean initialize) {
		super(new SingleFileCardStorage(file, location, initialize), false);
	}

	@Override
	public boolean doAddCard(IMagicCard card) {
		return getStorage().add(card);
	}

	@Override
	public boolean doRemoveCard(IMagicCard card) {
		return getStorage().remove(card);
	}
}
