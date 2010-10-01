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

import org.eclipse.core.runtime.IProgressMonitor;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhisical;

/**
 * export in format 4x Plain ...
 */
public class ClassicExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public ReportType getType() {
		return ReportType.TEXT_DECK_CLASSIC;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		PrintStream exportStream = new PrintStream(st);
		Location location = null;
		for (IMagicCard magicCard : store) {
			IMagicCard card = magicCard;
			String name;
			int count = 1;
			if (card instanceof MagicCardPhisical) {
				MagicCardPhisical mc = (MagicCardPhisical) card;
				if (location != mc.getLocation()) {
					location = mc.getLocation();
					exportStream.println("# " + location.getName());
				}
				name = mc.getName();
				count = mc.getCount();
			} else {
				name = card.getName();
			}
			String line = new Formatter().format("%2dx %s", count, name)
					.toString();
			exportStream.println(line);
			monitor.worked(1);
		}
		exportStream.close();
	}
}
