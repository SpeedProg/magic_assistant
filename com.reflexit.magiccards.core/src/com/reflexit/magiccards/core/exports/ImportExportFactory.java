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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.reflexit.magiccards.core.DataManager;

/**
 * Import/Export factory - gets instance of worker class by its type
 */
public class ImportExportFactory<T> {
	private static Map<ReportType, Object> importRegistry = new LinkedHashMap<ReportType, Object>();
	private static Map<ReportType, Object> exportRegistry = new LinkedHashMap<ReportType, Object>();

	public IImportDelegate<T> getImportWorker(ReportType type) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		if (importRegistry.size() == 0) {
			initRegistry();
		}
		IImportDelegate<T> newInstance;
		Object className = importRegistry.get(type);
		if (className instanceof IImportDelegate) {
			return (IImportDelegate) className;
		}
		if (className instanceof String) {
			Class loadClass = getClass().getClassLoader().loadClass((String) className);
			newInstance = (IImportDelegate<T>) loadClass.newInstance();
			importRegistry.put(type, newInstance);
			return newInstance;
		}
		return null;
	}

	public IExportDelegate<T> getExportWorker(ReportType type) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		if (exportRegistry.size() == 0) {
			initRegistry();
		}
		IExportDelegate<T> newInstance;
		Object className = exportRegistry.get(type);
		if (className instanceof IExportDelegate) {
			return (IExportDelegate<T>) className;
		}
		if (className instanceof String) {
			Class loadClass = getClass().getClassLoader().loadClass((String) className);
			newInstance = (IExportDelegate<T>) loadClass.newInstance();
			exportRegistry.put(type, newInstance);
			return newInstance;
		}
		return null;
	}

	private static void initRegistry() {
		loadExtensions();
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
		String id = elp.getAttribute("id");
		String label = elp.getAttribute("label");
		String imp = elp.getAttribute("importDelegate");
		String exp = elp.getAttribute("exportDelegate");
		String sxml = elp.getAttribute("xmlFormat");
		String ext = elp.getAttribute("extension");
		boolean xmlFormat = Boolean.valueOf(sxml);
		ReportType rt = ReportType.createReportType(id, label, ext, xmlFormat);
		addImportWorker(rt, imp);
		addExportWorker(rt, exp);
	}

	public static void addImportWorker(ReportType type, Object delegate) {
		if (delegate instanceof String || delegate instanceof IImportDelegate)
			importRegistry.put(type, delegate);
		else if (delegate == null)
			importRegistry.remove(type);
		else
			throw new ClassCastException();
	}

	public static void addExportWorker(ReportType type, Object delegate) {
		if (delegate instanceof String || delegate instanceof IExportDelegate)
			exportRegistry.put(type, delegate);
		else if (delegate == null)
			exportRegistry.remove(type);
		else
			throw new ClassCastException();
	}

	public static Collection<ReportType> getImportTypes() {
		if (importRegistry.size() == 0) {
			initRegistry();
		}
		return new ArrayList<ReportType>(importRegistry.keySet());
	}

	public static Collection<ReportType> getExportTypes() {
		if (exportRegistry.size() == 0) {
			initRegistry();
		}
		return new ArrayList<ReportType>(exportRegistry.keySet());
	}
}
