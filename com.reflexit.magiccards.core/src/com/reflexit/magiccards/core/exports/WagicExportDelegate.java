/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Wing�rd  - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.exports;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;

/**
 * Export to Wagic: The Homebrew (http://wololo.net/wagic/)
 * TODO: add description
 */
public class WagicExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public WagicExportDelegate() {
	}

	public String getName() {
		if (store instanceof ILocatable) {
			return ((ILocatable) store).getLocation().getName();
		}
		ICardStore store1 = store.getCardStore();
		if (store1 instanceof IStorageContainer) {
			IStorage storage = ((IStorageContainer) store1).getStorage();
			if (storage instanceof IStorageInfo) {
				IStorageInfo si = ((IStorageInfo) storage);
				return si.getName();
			}
		}
		return "deck";
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Exporting to wagic...", store.getSize());
		PrintStream stream = new PrintStream(st);
		String name = getName();
		stream.println("#NAME:" + name);
		for (IMagicCard magicCard : store) {
			if (magicCard instanceof MagicCardPhisical) {
				MagicCardPhisical phisical = ((MagicCardPhisical) magicCard);
				for (int i = 0; i < phisical.getCount(); i++)
					stream.println(phisical.getCardId());
			} else if (magicCard instanceof MagicCard) {
				MagicCard card = (MagicCard) magicCard;
				stream.println(card.getCardId());
			}
			monitor.worked(1);
		}
		stream.close();
		monitor.done();
	}

	public ReportType getType() {
		return ReportType.createReportType("wth", "Wagic: The Homebrew");
	}
}
