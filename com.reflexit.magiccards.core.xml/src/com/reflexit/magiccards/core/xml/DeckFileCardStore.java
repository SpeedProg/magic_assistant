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

import org.eclipse.core.runtime.Path;

import java.io.File;

import com.reflexit.magiccards.core.model.ICardDeck;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.CollectionCardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;

/**
 * @author Alena
 *
 */
public class DeckFileCardStore extends CollectionCardStore implements ICardDeck<IMagicCard>, ILocatable {
	/**
	 * @param file
	 */
	public DeckFileCardStore(File file, String name, String location) {
		this(new SingleFileCardStorage(file, location));
		if (name != null)
			setDeckName(name);
	}

	public DeckFileCardStore(SingleFileCardStorage storage) {
		super(storage);
		getDeckFileStorage().setType("deck");
		getDeckFileStorage().setName(
		        new Path(new Path(storage.getLocation()).lastSegment()).removeFileExtension().toString());
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.ICardDeck#getDeckComment()
	 */
	public String getDeckComment() {
		return getDeckFileStorage().getComment();
	}

	public void setDeckName(String name) {
		getDeckFileStorage().setName(name);
	}

	protected SingleFileCardStorage getDeckFileStorage() {
		return (SingleFileCardStorage) getStorage();
	}

	public void setDeckComment(String comment) {
		getDeckFileStorage().setComment(comment);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.ICardDeck#getDeckName()
	 */
	public String getDeckName() {
		return getDeckFileStorage().getName();
	}

	public String getLocation() {
		return getDeckFileStorage().getLocation();
	}

	public void setLocation(String location) {
		getDeckFileStorage().setLocation(location);
	}
}
