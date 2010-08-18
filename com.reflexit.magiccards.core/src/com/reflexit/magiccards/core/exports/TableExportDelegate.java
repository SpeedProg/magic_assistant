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
public class TableExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public void runTablePipeExport(IProgressMonitor monitor) throws InvocationTargetException {
		TableExporter exporter = null;
		try {
			exporter = new TableExporter(st, "|");
			exportToTable(monitor, store, exporter, header);
			;
		} finally {
			if (exporter != null)
				exporter.close();
		}
	}

	public ReportType getType() {
		return ReportType.TABLE_PIPED;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		runTablePipeExport(monitor);
	}
}
