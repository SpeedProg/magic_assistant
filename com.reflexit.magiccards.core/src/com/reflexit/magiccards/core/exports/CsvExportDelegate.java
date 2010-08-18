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

import java.lang.reflect.InvocationTargetException;

import com.reflexit.magiccards.core.model.IMagicCard;

/**
 * TODO: add description
 */
public class CsvExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public void runCsvExport(IProgressMonitor monitor) throws InvocationTargetException {
		CsvExporter exporter = null;
		try {
			exporter = new CsvExporter(st);
			exportToTable(monitor, store, exporter, header);
		} finally {
			if (exporter != null)
				exporter.close();
		}
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		runCsvExport(monitor);
	}

	public ReportType getType() {
		return ReportType.CSV;
	}
}
