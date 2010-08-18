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

import org.eclipse.core.runtime.IProgressMonitor;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;

/**
 * export in format 
 * 4x Plain
 * ...
 */
public class ClassicExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public ReportType getType() {
		return ReportType.TEXT_DECK_CLASSIC;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		PrintStream exportStream = new PrintStream(st);
		if (header) {
			exportStream.println("COUNT NAME");
		}
		for (IMagicCard magicCard : store) {
			IMagicCard card = magicCard;
			if (card instanceof MagicCardPhisical) {
				MagicCardPhisical mc = (MagicCardPhisical) card;
				exportStream.println(mc.getCount() + "x " + mc.getName());
			} else {
				exportStream.println("1x " + card.getName());
			}
			monitor.worked(1);
		}
		exportStream.close();
	}
}
