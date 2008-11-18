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
import java.util.HashSet;
import java.util.Iterator;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;

/**
 * @author Alena
 *
 */
public class VirtualMultiFileCardStore extends AbstractCardStoreWithStorage<IMagicCard> {
	public VirtualMultiFileCardStore() {
		super(new MultiFileCardStore());
	}

	/**
	 * @param file
	 * @param location
	 */
	public void addFile(File file, String location) {
		((MultiFileCardStore) this.storage).addFile(file, location);
	}

	@Override
	protected boolean doAddCard(IMagicCard card) {
		return this.storage.addCard(card);
	}

	@Override
	protected boolean doRemoveCard(IMagicCard card) {
		return this.storage.removeCard(card);
	}

	@Override
	protected void doInitialize() throws MagicException {
		this.storage.initialize();
		pruneDuplicates();
	}

	/**
	 * 
	 */
	private void pruneDuplicates() {
		HashSet<IMagicCard> hash = new HashSet<IMagicCard>();
		ArrayList<IMagicCard> duplicates = new ArrayList<IMagicCard>();
		for (Iterator iterator = cardsIterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (hash.contains(card)) {
				duplicates.add(card);
			}
			hash.add(card);
		}
		boolean old = this.storage.isAutoCommit();
		this.storage.setAutoSave(false);
		for (Iterator iterator = duplicates.iterator(); iterator.hasNext();) {
			IMagicCard name = (IMagicCard) iterator.next();
			this.storage.removeCard(name);
		}
		System.err.println("removed " + duplicates.size() + " duplicates");
		this.storage.setAutoSave(old);
		this.storage.save();
	}
}
