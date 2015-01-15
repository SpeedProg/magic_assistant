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

import java.lang.reflect.InvocationTargetException;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * export in format 4x Plain ...
 */
public class ClassicExportDelegate extends AbstractExportDelegatePerLine<IMagicCard> {
	@Override
	public void run(ICoreProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		setColumns(new ICardField[] { MagicCardField.COUNT, MagicCardField.NAME });
		super.run(monitor);
	}

	@Override
	public void printHeader() {
		// nothing
	}

	@Override
	public void printLine(Object[] values) {
		String line = String.format("%dx %s", values);
		stream.println(line);
	}

	@Override
	public void printLocationHeader() {
		if (header && printLocation) {
			stream.println("# " + location.getName());
		}
	}

	@Override
	public boolean isColumnChoiceSupported() {
		return false;
	}
}
