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

/**
 * export in format 4 Plain ...
 */
public class ClassicNoXExportDelegate extends ClassicExportDelegate {
	@Override
	public void printLine(Object[] values) {
		String line = String.format("%d %s", values);
		stream.println(line);
	}
}
