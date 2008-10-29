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
package com.reflexit.magiccards.core.model.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;

/**
 * @author Alena
 *
 */
public class MemoryCardStore extends AbstractCardStore<IMagicCard> {
	protected ArrayList<IMagicCard> list;

	/**
	 * 
	 */
	public MemoryCardStore() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.AbstractCardStore#doInitialize()
	 */
	@Override
	protected void doInitialize() throws MagicException {
		this.list = new ArrayList<IMagicCard>();
	}

	public Iterator<IMagicCard> cardsIterator() {
		return this.getList().iterator();
	}

	@Override
	protected void doAddAll(Collection cards) {
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard object = (IMagicCard) iterator.next();
			doAddCard(object);
		}
	}

	public int getTotal() {
		return this.getList().size();
	}

	@Override
	public void doRemoveCard(IMagicCard card) {
		this.getList().remove(card);
	}

	@Override
	public boolean doAddCard(IMagicCard card) {
		return this.getList().add(card);
	}

	/**
	 * @return the list
	 */
	public ArrayList<IMagicCard> getList() {
		if (this.list == null)
			doInitialize();
		return this.list;
	}

	public void save() {
		// do nothing
	}
}