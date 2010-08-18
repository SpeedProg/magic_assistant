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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.reflexit.magiccards.core.Activator;

/**
 * Import/Export factory - gets instance of worker class by its type
 */
public class ImportExportFactory<T> {
	private static Map<ReportType, String> importRegistry = new LinkedHashMap<ReportType, String>();
	private static Map<ReportType, String> exportRegistry = new LinkedHashMap<ReportType, String>();

	public IImportDelegate<T> getImportWorker(ReportType type) throws ClassNotFoundException, InstantiationException,
	        IllegalAccessException {
		if (importRegistry.size() == 0) {
			initRegistry();
		}
		IImportDelegate<T> newInstance;
		String className = importRegistry.get(type);
		if (className == null)
			return null;
		Class loadClass = getClass().getClassLoader().loadClass(className);
		newInstance = (IImportDelegate<T>) loadClass.newInstance();
		return newInstance;
	};

	public IExportDelegate<T> getExportWorker(ReportType type) throws ClassNotFoundException, InstantiationException,
	        IllegalAccessException {
		if (exportRegistry.size() == 0) {
			initRegistry();
		}
		IExportDelegate<T> newInstance;
		String className = exportRegistry.get(type);
		if (className == null)
			return null;
		Class loadClass = getClass().getClassLoader().loadClass(className);
		newInstance = (IExportDelegate<T>) loadClass.newInstance();
		return newInstance;
	};

	private void initRegistry() {
		loadExtensions();
	}

	private static void loadExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(Activator.PLUGIN_ID + ".deckFormat");
		IConfigurationElement points[] = extensionPoint.getConfigurationElements();
		for (IConfigurationElement el : points) {
			parseExtension(el);
		}
	}

	private static void parseExtension(IConfigurationElement elp) {
		String id = elp.getAttribute("id");
		String label = elp.getAttribute("label");
		String imp = elp.getAttribute("importDelegate");
		String exp = elp.getAttribute("exportDelegate");
		String sxml = elp.getAttribute("xmlFormat");
		boolean xmlFormat = Boolean.valueOf(sxml);
		ReportType rt = ReportType.createReportType(id, label, xmlFormat);
		addImportWorker(rt, imp);
		addExportWorker(rt, exp);
	}

	public static void addImportWorker(ReportType type, String clazz) {
		addWorker(importRegistry, type, clazz);
	}

	public static void addExportWorker(ReportType type, String clazz) {
		addWorker(exportRegistry, type, clazz);
	}

	private static void addWorker(Map<ReportType, String> registry, ReportType type, String clazz) {
		if (clazz != null)
			registry.put(type, clazz);
	}

	public Collection<ReportType> getImportTypes() {
		if (importRegistry.size() == 0) {
			initRegistry();
		}
		return new ArrayList<ReportType>(importRegistry.keySet());
	}

	public Collection<ReportType> getExportTypes() {
		if (exportRegistry.size() == 0) {
			initRegistry();
		}
		return new ArrayList<ReportType>(exportRegistry.keySet());
	}
}
