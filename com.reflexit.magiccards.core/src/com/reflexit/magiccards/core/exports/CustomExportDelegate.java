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
import java.util.Formatter;

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
	private ReportType rtype;

	public ReportType getType() {
		return rtype;
	}

	public CustomExportDelegate() {
		this(ReportType.createReportType("", "Custom Format", "txt", false));
	}

	public CustomExportDelegate(ReportType type) {
		rtype = type;
	}

	public void run(ICoreProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		String format = rtype.getProperty(ROW_FORMAT);
		String fields = rtype.getProperty(ROW_FIELDS);
		ICardField[] xfields = fields == null ? new ICardField[] { MagicCardFieldPhysical.COUNT, MagicCardField.NAME }
				: MagicCardFieldPhysical.toFields(fields, ",");
		if (format == null)
			format = "%2d %s";
		PrintStream exportStream = new PrintStream(st);
		Location location = null;
		for (IMagicCard card : store) {
			Object values[] = new Object[xfields.length];
			int i = 0;
			for (ICardField field : xfields) {
				values[i] = card.getObjectByField(field);
				i++;
			}
			if (card instanceof MagicCardPhysical) {
				Location curLocation = ((MagicCardPhysical) card).getLocation();
				if (location != curLocation) {
					location = ((MagicCardPhysical) card).getLocation();
					exportStream.println("# " + location.getName());
				}
			}
			String line = new Formatter().format(format, values).toString();
			exportStream.println(line);
			monitor.worked(1);
		}
		exportStream.close();
	}
}
