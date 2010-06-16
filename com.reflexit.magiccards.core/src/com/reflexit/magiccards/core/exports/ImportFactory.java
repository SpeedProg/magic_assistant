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

import java.util.Collection;
import java.util.HashMap;

import com.reflexit.magiccards.core.Activator;

/**
 * Import factory - gets instance of worker class by its type
 */
public class ImportFactory<T> {
	private static HashMap<ReportType, String> importRegistry = new HashMap<ReportType, String>();

	public IImportWorker<T> getImportWorker(ReportType type) throws ClassNotFoundException, InstantiationException,
	        IllegalAccessException {
		if (importRegistry.size() == 0) {
			initRegistry();
		}
		IImportWorker<T> newInstance;
		String className = importRegistry.get(type);
		if (className == null)
			return null;
		Class loadClass = getClass().getClassLoader().loadClass(className);
		newInstance = (IImportWorker<T>) loadClass.newInstance();
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
		ReportType rt = ReportType.createReportType(id, label);
		addWorker(rt, imp);
	}

	public static void addWorker(ReportType type, String clazz) {
		importRegistry.put(type, clazz);
	}

	public static void removeWorker(ReportType type) {
		importRegistry.remove(type);
	}

	public Collection<ReportType> getTypes() {
		if (importRegistry.size() == 0) {
			initRegistry();
		}
		return importRegistry.keySet();
	}
}
