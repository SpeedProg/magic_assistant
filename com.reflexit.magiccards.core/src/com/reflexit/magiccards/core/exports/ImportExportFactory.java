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
import java.util.Collection;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;

/**
 * Import/Export factory - gets instance of worker class by its type
 */
public class ImportExportFactory<T> {
	static {
		loadExtensions();
		loadCustom();
	}

	private static void loadCustom() {
		IPath exportersPath = ReportType.getStoragePath();
		File[] listFiles = exportersPath.toFile().listFiles();
		for (int i = 0; i < listFiles.length; i++) {
			File file = listFiles[i];
			try {
				ReportType.load(file);
			} catch (IOException e) {
				MagicLogger.log(e);
			}
		}
	}

	private static void loadExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(DataManager.ID + ".deckFormat");
		IConfigurationElement points[] = extensionPoint.getConfigurationElements();
		for (IConfigurationElement el : points) {
			parseExtension(el);
		}
	}

	private static void parseExtension(IConfigurationElement elp) {
		// String id = elp.getAttribute("id");
		String label = elp.getAttribute("label");
		String imp = elp.getAttribute("importDelegate");
		String exp = elp.getAttribute("exportDelegate");
		String sxml = elp.getAttribute("xmlFormat");
		String ext = elp.getAttribute("extension");
		boolean xmlFormat = Boolean.valueOf(sxml);
		ReportType rt = ReportType.createReportType(label, ext, xmlFormat);
		rt.setImportDelegate(imp);
		rt.setExportDelegate(exp);
	}

	public static Collection<ReportType> getImportTypes() {
		return ReportType.getImportTypes();
	}

	public static Collection<ReportType> getExportTypes() {
		return ReportType.getExportTypes();
	}
}
