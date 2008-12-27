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
import com.reflexit.magiccards.core.xml.data.CardCollectionStoreObject;

/**
 * @author Alena
 *
 */
public class DeckFileCardStore extends CollectionCardStore implements ICardDeck<IMagicCard>, ILocatable {
	static class DeckFileStorage extends SingleFileCardStorage {
		private String name;
		private String comment;

		/**
		 * @param file
		 */
		public DeckFileStorage(File file, String location) {
			super(file, location);
			setName(new Path(new Path(location).lastSegment()).removeFileExtension().toString());
		}

		@Override
		protected void loadFields(CardCollectionStoreObject obj) {
			super.loadFields(obj);
			if (obj.name != null)
				this.setName(obj.name);
			this.setComment(obj.comment);
			if (getLocation() == null) {
				setLocation("Decks/" + getName() + ".xml");
			}
		}

		@Override
		protected void storeFields(CardCollectionStoreObject obj) {
			obj.name = this.getName();
			obj.comment = this.getComment();
			obj.type = "deck";
			super.storeFields(obj);
		}

		void setComment(String comment) {
			this.comment = comment;
		}

		String getComment() {
			return comment;
		}

		void setName(String name) {
			this.name = name;
		}

		String getName() {
			return name;
		}
	}

	/**
	 * @param file
	 */
	public DeckFileCardStore(File file, String name, String location) {
		super(new DeckFileStorage(file, location));
		if (name != null)
			setDeckName(name);
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

	protected DeckFileStorage getDeckFileStorage() {
		return (DeckFileStorage) getStorage();
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
