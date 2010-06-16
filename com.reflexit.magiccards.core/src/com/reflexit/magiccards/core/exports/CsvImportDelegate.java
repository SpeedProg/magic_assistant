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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.reflexit.magiccards.core.model.MagicCardPhisical;

/**
 * Import for classic text deck format
 */
public class CsvImportDelegate extends ImportWorker {
	@Override
	public ReportType getType() {
		return ReportType.CSV;
	}

	public CsvImportDelegate() {
	}

	/**
	 * @param monitor
	 * @throws IOException 
	 */
	public void doRun(IProgressMonitor monitor) throws IOException {
		runCsvImport(monitor);
	}

	public void runCsvImport(IProgressMonitor monitor) throws IOException {
		try {
			CsvImporter importer = null;
			try {
				importer = new CsvImporter(getStream());
				do {
					line++;
					List<String> list = importer.readLine();
					if (list == null)
						break;
					if (isHeader() && line == 1)
						continue;
					MagicCardPhisical card = createCard(list);
					importCard(card);
					if (previewMode && line >= 10)
						break;
					monitor.worked(1);
				} while (true);
			} catch (FileNotFoundException e) {
				throw e;
			} finally {
				if (importer != null)
					importer.close();
			}
		} catch (IOException e) {
			throw e;
		}
	}
}
