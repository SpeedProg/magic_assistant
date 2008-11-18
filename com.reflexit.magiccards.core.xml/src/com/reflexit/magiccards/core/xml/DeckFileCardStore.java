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
import com.reflexit.magiccards.core.xml.data.CardCollectionStoreObject;

/**
 * @author Alena
 *
 */
public class DeckFileCardStore extends CollectionCardStore implements ICardDeck<IMagicCard> {
	static class DeckExtra extends SingleFileCardStore {
		String name;
		String comment;

		/**
		 * @param file
		 */
		public DeckExtra(File file) {
			super(file);
		}

		@Override
		protected void loadFields(CardCollectionStoreObject obj) {
			super.loadFields(obj);
			// do not load name, keep default for now
			this.comment = obj.comment;
		}

		@Override
		protected void storeFields(CardCollectionStoreObject obj) {
			obj.name = this.name;
			obj.comment = this.comment;
			super.storeFields(obj);
		}
	}

	/**
	 * @param file
	 */
	public DeckFileCardStore(File file, String name) {
		super(new DeckExtra(file));
		if (name != null)
			setDeckName(name);
		else {
			String base = file.getName();
			base = new Path(base).removeFileExtension().toString();
			setDeckName(base);
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.ICardDeck#getDeckComment()
	 */
	public String getDeckComment() {
		return getDeckExtra().comment;
	}

	public void setDeckName(String name) {
		getDeckExtra().name = name;
	}

	protected DeckExtra getDeckExtra() {
		return (DeckExtra) this.storage;
	}

	public void setDeckComment(String comment) {
		getDeckExtra().comment = comment;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.ICardDeck#getDeckName()
	 */
	public String getDeckName() {
		return getDeckExtra().name;
	}
}
