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

import java.io.OutputStream;
import java.text.MessageFormat;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * export in format 4x Plain ...
 */
public class CustomExportDelegate extends AbstractExportDelegatePerLine<IMagicCard> {
	public static final String ROW_FORMAT = "row.format";
	public static final String ROW_FIELDS = "row.fields";
	public static final String ROW_FORMAT_TYPE = "format.type";
	public static final String ROW_FORMAT_TYPE_NUM = "format.type.num";
	public static final String HEADER = "main.header";
	public static final String FOOTER = "main.footer";
	public static final String SB_HEADER = "sideboard.header";
	public static final String DECK_NAME_VAR = "${DECK.NAME}";
	public static final String SB_FIELD = "sideboard.field";
	public static final int FORMAT_JAVA = 0;
	public static final int FORMAT_PRINTF = 1;
	public static final int FORMAT_SEP = 2;
	public static final int FORMAT_SEP_QUOT = 3;
	private String format;
	private String headerStr;
	private String footerStr;
	private String sbHeaderStr;
	private int itype;
	private String sbFieldStr;

	public CustomExportDelegate() {
		this(ImportExportFactory.createReportType("Custom", "txt", false));
	}

	public CustomExportDelegate(ReportType type) {
		setReportType(type);
	}

	public static String[] getFormatLabels() {
		String[] values = new String[] { "Java Message Format", "C Printf Format", //
				"Separated Fields", "Separated with Quites Escape" };
		return values;
	}

	@Override
	public void init(OutputStream st, boolean header, IFilteredCardStore<IMagicCard> filteredLibrary) {
		super.init(st, header, filteredLibrary);
		ReportType rtype = getType();
		if (rtype == null)
			throw new MagicException("Exporter is not initialized");
		format = rtype.getProperty(ROW_FORMAT);
		String fields = rtype.getProperty(ROW_FIELDS);
		if (fields != null && fields.length() > 0)
			columns = MagicCardField.toFields(fields, ",");
		else if (columns == null) {
			columns = new ICardField[] { MagicCardField.COUNT, MagicCardField.NAME };
		}
		headerStr = rtype.getProperty(HEADER);
		footerStr = rtype.getProperty(FOOTER);
		sbHeaderStr = rtype.getProperty(SB_HEADER);
		sbFieldStr = rtype.getProperty(SB_FIELD);
		String ftype = rtype.getProperty(ROW_FORMAT_TYPE_NUM);
		itype = 1;
		if (ftype != null) {
			itype = Integer.parseInt(ftype);
		}
		if (format == null) {
			if (isFieldSeparated())
				format = ",";
			else
				format = "%d %s";
		}
	}

	@Override
	public void printHeader() {
		if (isFieldSeparated()) {
			super.printHeader();
		} else if (header && headerStr != null && headerStr.length() > 0) {
			String deckName = location.getName();
			String xheader = headerStr.replace(DECK_NAME_VAR, deckName);
			stream.println(xheader);
		}
	}

	@Override
	public String getSeparator() {
		return format;
	}

	@Override
	public void printLocationHeader() {
		if (location.isSideboard() && sbHeaderStr != null && sbHeaderStr.length() > 0 && !isFieldSeparated()) {
			String deckName = location.getName();
			String xheader = sbHeaderStr.replace(DECK_NAME_VAR, deckName);
			stream.println(xheader);
		}
	}

	@Override
	public void printFooter() {
		if (header && footerStr != null && footerStr.length() > 0 && !isFieldSeparated()) {
			String deckName = location.getName();
			String xheader = footerStr.replace(DECK_NAME_VAR, deckName);
			stream.println(xheader);
		}
	}

	protected boolean isFieldSeparated() {
		return itype == FORMAT_SEP || itype == FORMAT_SEP_QUOT;
	}

	@Override
	public void printLine(Object[] values) {
		String line;
		if (itype == FORMAT_JAVA) {
			line = new MessageFormat(format).format(values);
		} else if (itype == FORMAT_PRINTF) {
			line = String.format(format, values);
		} else {
			super.printLine(values);
			return;
		}
		stream.println(line);
	}

	@Override
	public Object getObjectByField(IMagicCard card, ICardField field) {
		Object value = super.getObjectByField(card, field);
		if (field == MagicCardField.SIDEBOARD && sbFieldStr != null && sbFieldStr.length() > 0) {
			String choice[] = sbFieldStr.split("/");
			if (Boolean.valueOf(value.toString())) {
				return choice[0];
			} else {
				if (choice.length > 1) {
					return choice[1];
				} else {
					return "";
				}
			}
		}
		if (value == null)
			return "";
		return value;
	}

	@Override
	protected String escape(String element) {
		switch (itype) {
			case FORMAT_SEP:
				break;
			case FORMAT_SEP_QUOT:
				return escapeQuot(element);
			default:
				break;
		}
		return element;
	}
}
