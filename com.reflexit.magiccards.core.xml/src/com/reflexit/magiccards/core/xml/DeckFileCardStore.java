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

/**
 * @author Alena
 *
 */
public class DeckFileCardStore extends LibraryCardStore implements ICardDeck<IMagicCard> {
	/**
	 * @param file
	 */
	public DeckFileCardStore(File file, String name) {
		super(file);
		if (name != null)
			this.name = name;
		else {
			String base = file.getName();
			base = new Path(base).removeFileExtension().toString();
			this.name = base;
		}
	}
	protected String name;
	protected String comment;

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.ICardDeck#getDeckComment()
	 */
	public String getDeckComment() {
		return this.comment;
	}

	public void setDeckName(String name) {
		this.name = name;
	}

	public void setDeckComment(String comment) {
		this.comment = comment;
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.ICardDeck#getDeckName()
	 */
	public String getDeckName() {
		return this.name;
	}
}
