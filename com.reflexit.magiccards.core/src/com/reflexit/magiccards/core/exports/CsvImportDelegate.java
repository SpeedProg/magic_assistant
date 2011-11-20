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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Import for classic text deck format
 */
public class CsvImportDelegate extends AbstractImportDelegate {
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
	@Override
	public void doRun(ICoreProgressMonitor monitor) throws IOException {
		runCsvImport(monitor);
	}

	public char getSeparator() {
		return ',';
	}

	public void runCsvImport(ICoreProgressMonitor monitor) throws IOException {
		try {
			CsvImporter importer = null;
			try {
				importer = new CsvImporter(getStream(), getSeparator());
				do {
					line++;
					List<String> list = importer.readLine();
					if (list == null)
						break;
					if (line == 1 && isHeader()) {
						setHeaderFields(list);
						continue;
					}
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

	protected void setHeaderFields(List<String> list) {
		ICardField fields[] = new ICardField[list.size()];
		for (int i = 0; i < list.size(); i++) {
			String hd = list.get(i);
			ICardField field = MagicCardFieldPhysical.fieldByName(hd);
			fields[i] = field;
		}
		setFields(fields);
	}
}
