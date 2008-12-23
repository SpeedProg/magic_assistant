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

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;

/**
 * @author Alena
 * 
 */
public class VirtualMultiFileCardStore extends AbstractCardStoreWithStorage<IMagicCard> {
	public VirtualMultiFileCardStore() {
		super(new MultiFileCardStorage());
	}

	/**
	 * @param file
	 * @param location
	 */
	public void addFile(final File file, final String location, boolean initialize) {
		((MultiFileCardStorage) this.storage).addFile(file, location, initialize);
		if (initialize)
			initialized = initialize;
	}

	@Override
	protected synchronized boolean doAddAll(Collection<? extends IMagicCard> col) {
		boolean modified = super.doAddAll(col);
		pruneDuplicates();
		return modified;
	}

	/**
	 * 
	 */
	public void pruneDuplicates() {
		HashSet<Integer> hash = new HashSet<Integer>();
		ArrayList<IMagicCard> duplicates = new ArrayList<IMagicCard>();
		for (Object element : this) {
			IMagicCard card = (IMagicCard) element;
			if (hash.contains(card.getCardId())) {
				duplicates.add(card);
				continue;
			}
			hash.add(card.getCardId());
			if (Editions.getInstance().getAbbrByName(card.getEdition()) == null) {
				System.err.println("Failed to find set: " + card.getEdition());
			}
		}
		this.storage.removeAll(duplicates);
	}
}
