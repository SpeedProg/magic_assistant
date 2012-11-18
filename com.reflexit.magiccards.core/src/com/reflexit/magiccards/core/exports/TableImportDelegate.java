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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Import for table piped import
 */
public class TableImportDelegate extends AbstractImportDelegate {
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
	public void doRun(ICoreProgressMonitor monitor) throws IOException {
		runTablePipedImport(monitor);
	}

	public void runTablePipedImport(ICoreProgressMonitor monitor) throws IOException {
		try {
			BufferedReader importer = null;
			try {
				importer = new BufferedReader(new InputStreamReader(getStream()));
				do {
					line++;
					String input = importer.readLine();
					if (input == null)
						break;
					input = input.trim();
					String[] split = input.split("\\|");
					if (split.length > 1) {
						if (line == 1 && isHeader()) {
							ICardField fields[] = new ICardField[split.length];
							for (int i = 0; i < split.length; i++) {
								String hd = split[i];
								ICardField field = MagicCardFieldPhysical.fieldByName(hd);
								fields[i] = field;
							}
							setFields(fields);
							continue;
						}
						MagicCardPhysical card = createCard(Arrays.asList(split));
						importCard(card);
					} else {
						throw new IllegalArgumentException("Error: Line " + line + ". Fields seprated by | are not found: " + input);
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
