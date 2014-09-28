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

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Import for CVS deck format
 */
public class CsvImportDelegate extends TableImportDelegate {
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

	@Override
	public char getSeparator() {
		return ',';
	}

	public void runCsvImport(ICoreProgressMonitor monitor) throws IOException {
		try {
			CsvImporter importer = null;
			try {
				importer = new CsvImporter(getStream(), getSeparator());
				do {
					lineNum++;
					List<String> list = importer.readLine();

					if (list == null)
						break;
					if (list.size() < 2) {
						throw new MagicException("Line " + lineNum + ": Format error, at least 2 fields are expected");
					}
					if (lineNum == 1 && isHeader()) {
						setHeaderFields(list);
						continue;
					}
					MagicCardPhysical card = createCard(list);
					if (card != null)
						importCard(card);
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
