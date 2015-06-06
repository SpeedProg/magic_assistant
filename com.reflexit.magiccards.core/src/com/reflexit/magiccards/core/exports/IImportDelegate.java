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

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Import delegate interface
 */
public interface IImportDelegate {
	public ReportType getType();

	public void init(InputStream st, ImportData result);

	public void setHeader(boolean header);

	public void run(ICoreProgressMonitor monitor) throws InvocationTargetException, InterruptedException;

	public ImportData getResult();

	public void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value);

	public void setReportType(ReportType reportType);
}