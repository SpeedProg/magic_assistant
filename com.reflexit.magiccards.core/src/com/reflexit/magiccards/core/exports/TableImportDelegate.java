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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import com.reflexit.magiccards.core.model.MagicCardPhisical;

/**
 * Import for table piped import
 */
public class TableImportDelegate extends ImportWorker {
	@Override
	public ReportType getType() {
		return ReportType.TABLE_PIPED;
	}

	public TableImportDelegate() {
	}

	/**
	 * @param monitor
	 * @throws IOException 
	 */
	public void doRun(IProgressMonitor monitor) throws IOException {
		runTablePipedImport(monitor);
	}

	public void runTablePipedImport(IProgressMonitor monitor) throws IOException {
		try {
			BufferedReader importer = null;
			try {
				importer = new BufferedReader(new InputStreamReader(getStream()));
				do {
					line++;
					String input = importer.readLine();
					if (input == null)
						break;
					String[] split = input.split("\\|");
					if (split.length > 1) {
						MagicCardPhisical card = createCard(Arrays.asList(split));
						importCard(card);
					} else {
						throw new IllegalArgumentException("Error: Line " + line
						        + ". Fields seprated by | are not found: " + input);
					}
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
