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
import java.lang.reflect.InvocationTargetException;

import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Import delegate interface
 */
public interface IExportDelegate<T> {
	public ReportType getType();

	public void init(OutputStream st, boolean header, IFilteredCardStore<T> cards);

	public void run(ICoreProgressMonitor monitor) throws InvocationTargetException, InterruptedException;

	public void setColumns(ICardField[] columnsForExport);

	public void setReportType(ReportType reportType);

	public boolean isSideboardSupported();

	public boolean isColumnChoiceSupported();

	public boolean isMultipleLocationSupported();

	public String getExample();
}