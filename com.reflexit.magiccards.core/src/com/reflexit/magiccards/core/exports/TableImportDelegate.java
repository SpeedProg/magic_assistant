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
import java.util.List;

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

	public char getSeparator() {
		return '|';
	}

	/**
	 * @param monitor
	 * @throws IOException
	 */
	@Override
	public void doRun(ICoreProgressMonitor monitor) throws IOException {
		try {
			BufferedReader importer = null;
			try {
				importer = new BufferedReader(new InputStreamReader(getStream()));
				do {
					lineNum++;
					String input = importer.readLine();
					if (input == null)
						break;
					input = input.trim();
					String[] split = input.split("\\Q" + getSeparator());
					if (split.length > 1) {
						if (lineNum == 1 && isHeader()) {
							setHeaderFields(split);
							continue;
						}
						MagicCardPhysical card = createCard(Arrays.asList(split));
						importCard(card);
					} else {
						throw new IllegalArgumentException("Error: Line " + lineNum + ". Fields seprated by | are not found: " + input);
					}
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

	protected MagicCardPhysical createCard(List<String> list) {
		MagicCardPhysical card = createDefaultCard();
		ICardField[] fields = getFields();
		for (int i = 0; i < fields.length && i < list.size(); i++) {
			ICardField f = fields[i];
			String value = list.get(i);
			if (value != null && value.length() > 0 && f != null) {
				try {
					setFieldValue(card, f, i, value.trim());
				} catch (Exception e) {
					card.setError(ImportError.createFieldNotSetError(f, e));
				}
			}
		}
		return card;
	}

	public ICardField[] getFields() {
		return importResult.getFields();
	}

	public void setHeaderFields(String[] split) {
		ICardField fields[] = new ICardField[split.length];
		for (int i = 0; i < split.length; i++) {
			String hd = split[i];
			ICardField field = MagicCardFieldPhysical.fieldByName(hd);
			fields[i] = field;
		}
		setFields(fields);
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

	public void setFields(ICardField[] fields) {
		importResult.setFields(fields);
	}
}
