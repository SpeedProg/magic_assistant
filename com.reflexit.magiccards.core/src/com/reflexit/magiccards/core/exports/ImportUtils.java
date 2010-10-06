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

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * Utils to perform import
 */
public class ImportUtils {
	public static void performImport(InputStream st, ReportType reportType, boolean header, Location location, ICardStore cardStore,
			IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (st != null) {
			IFilteredCardStore magicDbHandler = DataManager.getCardHandler().getDatabaseHandler();
			((AbstractFilteredCardStore<IMagicCard>) magicDbHandler).getSize(); // force
																				// initialization
			IImportDelegate worker;
			try {
				worker = new ImportExportFactory<IMagicCard>().getImportWorker(reportType);
			} catch (Exception e) {
				throw new InvocationTargetException(e);
			}
			worker.init(st, false, location, magicDbHandler.getCardStore());
			worker.setHeader(header);
			worker.run(monitor);
			Collection importedCards = worker.getImportedCards();
			Collection<Location> importedLocations = getLocations(importedCards);
			createDecks(importedLocations);
			cardStore.addAll(importedCards);
		}
	}

	private static void createDecks(Collection<Location> importedLocations) {
		for (Iterator iterator = importedLocations.iterator(); iterator.hasNext();) {
			Location location = (Location) iterator.next();
			if (location.isSideboard()) {
				ModelRoot root = DataManager.getModelRoot();
				String containerName = location.getParent().getPath();
				final CardElement resource = root.findElement(new Path(containerName));
				if (!(resource instanceof CollectionsContainer)) {
					continue; // ???
				}
				CollectionsContainer parent = (CollectionsContainer) resource;
				if (parent.contains(location)) {
					continue;
				}
				CardCollection sideboard = parent.addDeck(location.getBaseFileName());
				CardCollection maindeck = (CardCollection) parent.findChield(location.toMainDeck());
				if (maindeck != null)
					sideboard.setVirtual(maindeck.isVirtual());
				sideboard.close();
			}
		}
	}

	private static Collection<Location> getLocations(Collection importedCards) {
		HashSet<Location> res = new HashSet<Location>();
		for (Iterator iterator = importedCards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (card instanceof MagicCardPhisical)
				res.add(((MagicCardPhisical) card).getLocation());
		}
		return res;
	}

	public static void performImport(InputStream st, ReportType reportType, boolean header, Location location, IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		IFilteredCardStore filteredLibrary = DataManager.getCardHandler().getMyCardsHandler();
		performImport(st, reportType, header, location, filteredLibrary.getCardStore(), monitor);
	}

	public static PreviewResult performPreview(InputStream st, ReportType reportType, boolean header, IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		IImportDelegate worker;
		try {
			worker = new ImportExportFactory<IMagicCard>().getImportWorker(reportType);
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
		IFilteredCardStore magicDbHandler = DataManager.getCardHandler().getDatabaseHandler();
		worker.init(st, true, null, magicDbHandler.getCardStore());
		worker.setHeader(header);
		// init preview
		PreviewResult previewResult = worker.getPreview();
		worker.run(monitor);
		return previewResult;
	}
}
