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
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;

/**
 * Export of magic assistant csv
 */
public class CsvExportDelegate extends AbstractExportDelegatePerLine<IMagicCard> {
	@Override
	protected boolean isForExport(ICardField field) {
		return super.isForExport(field) || field == MagicCardField.SIDEBOARD;
	}

	@Override
	public String getSeparator() {
		return ",";
	}

	@Override
	protected String escape(String element) {
		return escapeQuot(element);
	}
}
