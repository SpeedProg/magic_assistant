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

import org.eclipse.core.runtime.IProgressMonitor;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * Utils to perform import
 */
public class ImportUtils {
	public static void performImport(InputStream st, ReportType reportType, boolean header, HashMap filter,
	        IFilteredCardStore filteredLibrary, IProgressMonitor monitor) throws InvocationTargetException,
	        InterruptedException {
		if (st != null) {
			// because we support both deck and collection it is trying to import using my cards handler
			// with specific filter set on which deck/collection it is. It is really ugly and card should 
			// have location set otherwise it is not adding them properly
			IFilteredCardStore magicDbHandler = DataManager.getCardHandler().getDatabaseHandler();
			((AbstractFilteredCardStore<IMagicCard>) magicDbHandler).getSize(); // force initialization
			MagicCardFilter old = filteredLibrary.getFilter();
			try {
				MagicCardFilter locFilter = new MagicCardFilter();
				locFilter.update(filter);
				filteredLibrary.update(locFilter);
				Location location = filteredLibrary.getLocation();
				IImportDelegate worker;
				try {
					worker = new ImportFactory<IMagicCard>().getImportWorker(reportType);
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
				worker.init(st, false, location, magicDbHandler.getCardStore());
				worker.setHeader(header);
				worker.run(monitor);
				ICardStore cardStore = filteredLibrary.getCardStore();
				cardStore.addAll(worker.getImportedCards());
			} finally {
				filteredLibrary.update(old); // restore filter
			}
		}
	}

	public static void performImport(InputStream st, ReportType reportType, boolean header, HashMap filter,
	        IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		IFilteredCardStore filteredLibrary = DataManager.getCardHandler().getMyCardsHandler();
		performImport(st, reportType, header, filter, filteredLibrary, monitor);
	}

	public static PreviewResult performPreview(InputStream st, ReportType reportType, boolean header,
	        IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		IImportDelegate worker;
		try {
			worker = new ImportFactory<IMagicCard>().getImportWorker(reportType);
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
