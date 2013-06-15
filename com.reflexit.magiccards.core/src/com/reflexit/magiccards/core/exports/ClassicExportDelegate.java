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
 * export in format 4x Plain ...
 */
public class ClassicExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public ReportType getType() {
		return ReportType.TEXT_DECK_CLASSIC;
	}

	@Override
	public void printLine(Object[] values) {
		String line = String.format("%2dx %s", values);
		stream.println(line);
	}

	@Override
	public void printLocationHeader() {
		stream.println("# " + location.getName());
	}
}
