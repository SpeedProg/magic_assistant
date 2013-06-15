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

import com.reflexit.magiccards.core.model.IMagicCard;

/**
 * Pipe separated table
 */
public class TableExportDelegate extends AbstractExportDelegate<IMagicCard> {
	private final String SEP = "|";

	public ReportType getType() {
		return ReportType.TABLE_PIPED;
	}

	@Override
	public String getSeparator() {
		return SEP;
	}

	@Override
	protected String escape(String element) {
		if (element.contains(SEP)) {
			return element.replaceAll("\\Q" + SEP, "?");
		}
		return element;
	}
}
