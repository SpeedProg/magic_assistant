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

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * export in format 4x Plain ...
 */
public class CustomExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public static final String ROW_FORMAT = "row.format";
	public static final String ROW_FIELDS = "row.fields";
	public static final String ROW_FORMAT_TYPE = "format.type";
	public static final String ROW_FORMAT_TYPE_NUM = "format.type.num";
	public static final String HEADER = "main.header";
	public static final String FOOTER = "main.footer";
	public static final String SB_HEADER = "sideboard.header";
	public static final String DECK_NAME_VAR = "${DECK.NAME}";
	private ReportType rtype;

	public ReportType getType() {
		return rtype;
	}

	public CustomExportDelegate() {
		this(ReportType.createReportType("Custom", "txt", false));
	}

	public CustomExportDelegate(ReportType type) {
		rtype = type;
	}

	public void run(ICoreProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		String format = rtype.getProperty(ROW_FORMAT);
		String fields = rtype.getProperty(ROW_FIELDS);
		String headerStr = rtype.getProperty(HEADER);
		String footerStr = rtype.getProperty(FOOTER);
		String sbHeaderStr = rtype.getProperty(SB_HEADER);
		String ftype = rtype.getProperty(ROW_FORMAT_TYPE_NUM);
		int itype = 0;
		if (ftype != null) {
			itype = Integer.valueOf(ftype);
		}
		ICardField[] xfields = fields == null ? new ICardField[] { MagicCardFieldPhysical.COUNT, MagicCardField.NAME }
				: MagicCardFieldPhysical.toFields(fields, ",");
		PrintStream exportStream = new PrintStream(stream);
		Location location = Location.NO_WHERE;
		for (IMagicCard card : store) {
			Object values[] = new Object[xfields.length];
			int i = 0;
			for (ICardField field : xfields) {
				values[i] = card.getObjectByField(field);
				if (values[i] == null)
					values[i] = "";
				i++;
			}
			if (card instanceof MagicCardPhysical) {
				Location curLocation = ((MagicCardPhysical) card).getLocation();
				if (location != curLocation) {
					location = ((MagicCardPhysical) card).getLocation();
					String deckName = location.getName();
					if (location.isSideboard() && sbHeaderStr != null) {
						String xheader = sbHeaderStr.replace(DECK_NAME_VAR, deckName);
						exportStream.println(xheader);
					} else {
						if (header && headerStr != null) {
							String xheader = headerStr.replace(DECK_NAME_VAR, deckName);
							exportStream.println(xheader);
						}
					}
				}
			}
			String line;
			if (itype == 0) {
				try {
					line = new MessageFormat(format).format(values);
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			} else if (itype == 1) {
				try {
					line = String.format(format, values);
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			} else {
				line = "";
				for (int j = 0; j < values.length; j++) {
					Object object = values[j];
					if (j > 0)
						line += format;
					line += object;
				}
			}
			exportStream.println(line);
			monitor.worked(1);
		}
		if (header && footerStr != null) {
			String deckName = location.getName();
			String xheader = footerStr.replace(DECK_NAME_VAR, deckName);
			exportStream.println(xheader);
		}
		exportStream.close();
	}
}
