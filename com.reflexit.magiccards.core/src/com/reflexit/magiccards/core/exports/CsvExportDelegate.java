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

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;

/**
 * Export of magic assistant csv
 */
public class CsvExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public ReportType getType() {
		return ReportType.CSV;
	}

	@Override
	protected boolean isForExport(ICardField field) {
		return super.isForExport(field) || field == MagicCardFieldPhysical.SIDEBOARD;
	}

	@Override
	public String getSeparator() {
		return ",";
	}

	@Override
	public String escape(String str) {
		// fields containing " must be in quotes and all " changed to ""
		if (str.indexOf('"') >= 0) {
			return "\"" + str.replaceAll("\"", "\"\"") + "\"";
		}
		// fields containing carriage return must be surrounded by double quotes
		if (str.indexOf('\n') >= 0)
			return "\"" + str + "\"";
		// fields that contain , must be surrounded by double quotes
		if (str.indexOf(',') >= 0)
			return "\"" + str + "\"";
		// fields starts or ends with spaces must be in double quotes
		if (str.startsWith(" ") || str.endsWith(" "))
			return "\"" + str + "\"";
		return str;
	}
}
