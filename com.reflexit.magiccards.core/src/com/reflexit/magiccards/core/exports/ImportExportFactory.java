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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Import/Export factory - gets instance of worker class by its type
 */
public class ImportExportFactory {
	private static Map<String, ReportType> types;
	public static final ReportType XML;
	public static final ReportType CSV;
	public static final ReportType TEXT_DECK_CLASSIC;
	public static final ReportType TABLE_PIPED;

	static {
		types = new LinkedHashMap<String, ReportType>();
		loadExtensions();
		loadCustom();
		XML = getByLabel("Magic Assistant XML");
		CSV = getByLabel("Magic Assistant CSV");
		TEXT_DECK_CLASSIC = getByLabel("Deck Classic Text: 1 x Name");
		TABLE_PIPED = getByLabel("Magic Assistant Piped Table");
	}

	private static void loadCustom() {
		File exportersPath = ReportType.getStorageFile();
		File[] listFiles = exportersPath.listFiles();
		if (listFiles != null) {
			for (int i = 0; i < listFiles.length; i++) {
				File file = listFiles[i];
				try {
					ReportType.load(file);
				} catch (IOException e) {
					MagicLogger.log(e);
				}
			}
		}
	}

	private static void loadExtensions() {
		try {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			if (registry != null) {
				IExtensionPoint extensionPoint = registry.getExtensionPoint(DataManager.ID + ".deckFormat");
				IConfigurationElement points[] = extensionPoint.getConfigurationElements();
				for (IConfigurationElement el : points) {
					parseExtension(el);
				}
			}
		} catch (Throwable e) {
			MagicLogger.log(e);
		}
	}

	private synchronized static void parseExtension(IConfigurationElement elp) {
		// String id = elp.getAttribute("id");
		String label = elp.getAttribute("label");
		String ext = elp.getAttribute("extension");
		String imp = elp.getAttribute("importDelegate");
		String exp = elp.getAttribute("exportDelegate");
		String sxml = elp.getAttribute("xmlFormat");
		boolean xmlFormat = Boolean.valueOf(sxml);
		createReportType(label, ext, xmlFormat, imp, exp);
	}

	public static ReportType createReportType(String label, String fileExtension, boolean xmlFormat, String importClass,
			String exportClass) {
		ReportType rt = createReportType(label, fileExtension, xmlFormat);
		rt.setImportDelegate(importClass);
		rt.setExportDelegate(exportClass);
		return rt;
	}

	public static Collection<ReportType> getImportTypes() {
		ArrayList<ReportType> res = new ArrayList<ReportType>();
		for (Iterator iterator = types.values().iterator(); iterator.hasNext();) {
			ReportType type = (ReportType) iterator.next();
			if (type.importWorker != null)
				res.add(type);
		}
		return res;
	}

	public static Collection<ReportType> getExportTypes() {
		ArrayList<ReportType> res = new ArrayList<ReportType>();
		for (Iterator iterator = types.values().iterator(); iterator.hasNext();) {
			ReportType type = (ReportType) iterator.next();
			if (type.exportWorker != null)
				res.add(type);
		}
		return res;
	}

	public static ReportType createReportType(String label) {
		return createReportType(label, "txt", false);
	}

	public static synchronized ReportType createReportType(String label, String extension, boolean xml) {
		ReportType reportType = types.get(label);
		if (reportType != null)
			return reportType;
		reportType = new ReportType(label, xml, extension);
		types.put(label, reportType);
		return reportType;
	}

	public static ReportType getByLabel(String label) {
		if (label == null)
			return null;
		return types.get(label);
	}

	static void remove(String label) {
		types.remove(label);
	}

	public static IFilteredCardStore getExampleData() {
		MemoryFilteredCardStore fstore = new MemoryFilteredCardStore<>();
		IImportDelegate del = new TableImportDelegate();
		del.init(new ImportData(true, Location.NO_WHERE, TableExportDelegate.getTablePiped()));
		try {
			del.run(ICoreProgressMonitor.NONE);
			fstore.addAll(del.getResult().getList());
			fstore.update();
		} catch (InvocationTargetException | InterruptedException e) {
			MagicLogger.log(e);
		}
		return fstore;
	}
}
