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
package com.reflexit.magiccards.core.exports;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Xml Import
 */
public class XmlImportDelegate extends AbstractImportDelegate {
	/**
	 * @param monitor
	 * @throws IOException
	 */
	@Override
	public void doRun(ICoreProgressMonitor monitor) throws IOException {
		try {
			File tmp = File.createTempFile("magic", ".xml");
			tmp.deleteOnExit();
			try {
				FileUtils.saveStream(getStream(), tmp);
				ICardStore store = DataManager.getInstance().getCardHandler()
						.loadFromXml(tmp.getAbsolutePath());
				// IStorage<IMagicCard> storage = ((IStorageContainer<IMagicCard>)
				// store).getStorage();
				// Location location = storage.getLocation();
				Iterator iterator = store.iterator();
				while (iterator.hasNext()) {
					lineNum++;
					Object next = iterator.next();
					if (next instanceof MagicCardPhysical) {
						MagicCardPhysical card = (MagicCardPhysical) next;
						importCard(card);
						card.setLocation(getLocation());
					} else if (next instanceof MagicCard) {
						MagicCardPhysical card = createDefaultCard();
						card.setMagicCard((MagicCard) next);
						importCard(card);
					}
					monitor.worked(1);
				}
			} catch (IOException e) {
				throw e;
			} finally {
				tmp.delete();
			}
		} catch (IOException e) {
			throw e;
		}
	}
}
