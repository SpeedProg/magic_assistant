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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;

/**
 * @author Alena
 * 
 */
public class VirtualMultiFileCardStore extends
		AbstractCardStoreWithStorage<IMagicCard> {
	public VirtualMultiFileCardStore() {
		super(new MultiFileCardStorage());
	}

	/**
	 * @param file
	 * @param location
	 */
	public void addFile(final File file, final String location) {
		((MultiFileCardStorage) this.storage).addFile(file, location);
		initialized = false;
	}

	@Override
	protected synchronized void doAddAll(final Collection<IMagicCard> col) {
		this.storage.addAll(col);
		pruneDuplicates();
	}

	/**
	 * 
	 */
	public void pruneDuplicates() {
		HashSet<Integer> hash = new HashSet<Integer>();
		ArrayList<IMagicCard> duplicates = new ArrayList<IMagicCard>();
		for (Iterator iterator = cardsIterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (hash.contains(card.getCardId())) {
				duplicates.add(card);
			}
			hash.add(card.getCardId());
		}
		boolean old = this.storage.isAutoCommit();
		this.storage.setAutoCommit(false);
		// System.err.println("removed " + duplicates.size() + " duplicates");
		for (Object element : duplicates) {
			IMagicCard name = (IMagicCard) element;
			this.storage.removeCard(name);
		}
		this.storage.setAutoCommit(old);
		if (duplicates.size() > 0)
			this.storage.save();
	}
}
