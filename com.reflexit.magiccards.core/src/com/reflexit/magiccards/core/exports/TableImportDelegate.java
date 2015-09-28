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

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.TextPrinter;

/**
 * Import for table piped import
 */
public class TableImportDelegate extends AbstractImportDelegate {
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
					if (input.isEmpty())
						continue;
					String[] split = input.split("\\Q" + getSeparator());
					if (split.length > 1) {
						if (lineNum == 1) {
							setHeaderFields(split);
							continue;
						}
						MagicCardPhysical card = createCard(Arrays.asList(split));
						importCard(card);
					} else {
						throw new IllegalArgumentException("Error: Line " + lineNum
								+ ". Fields seprated by | are not found: " + input);
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
			if (f == null) continue;
			String value = list.get(i);
			if (value != null && value.length() > 0) {
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
		return importData.getFields();
	}

	public void setHeaderFields(String[] split) {
		ICardField fields[] = new ICardField[split.length];
		for (int i = 0; i < split.length; i++) {
			String hd = split[i];
			ICardField field = getFieldByName(hd);
			fields[i] = field;
		}
		setFields(fields);
	}

	protected void setHeaderFields(List<String> list) {
		int nulls = 0;
		ICardField fields[] = new ICardField[list.size()];
		for (int i = 0; i < list.size(); i++) {
			String hd = list.get(i);
			ICardField field = getFieldByName(hd);
			fields[i] = field;
			if (field == null)
				nulls++;
		}
		if (nulls == list.size()) {
			ICardField[] allNonTransientFields = MagicCardField.allNonTransientFields(true);
			String hfields = TextPrinter.join(Arrays.asList(allNonTransientFields), ',');
			throw new MagicException("Cannot recognize header fields: " + list + ", expecting some of these "
					+ hfields);
		}
		setFields(fields);
	}

	protected ICardField getFieldByName(String th) {
		return MagicCardField.fieldByName(th);
	}

	public void setFields(ICardField[] fields) {
		importData.setFields(fields);
	}
}
