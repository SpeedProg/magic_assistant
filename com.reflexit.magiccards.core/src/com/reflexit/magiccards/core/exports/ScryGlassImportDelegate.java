/*******************************************************************************
 * Copyright (c) 2016 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Import for classic text deck format
 */
public class ScryGlassImportDelegate extends AbstractImportDelegate {
	public ScryGlassImportDelegate() {
	}

	/**
	 * @param monitor
	 * @throws IOException
	 */
	@Override
	public void doRun(ICoreProgressMonitor monitor) throws IOException {
		runDeckImport(monitor);
	}

	/*-
	1 Library of Lat-Nam [6E]
	3 Zur's Weirding [6E]
	1 Garza Zol, Plague Queen [CS]
	1 Garza's Assassin [CS]
	1 Sek'Kuar, Deathkeeper [CS]
	1 Endrek Sahr, Master Breeder [MM2]
	2 Thousand-Year Elixir [C13]
	1 Æther Gale [C14]
	1 Æther Snap [C14]
	1 Blue Sun's Zenith [C15]
	1 Overwhelming Stampede [C15]
	1 Edric, Spymaster of Trest [CRS]
	1 Æther Snap [DS]
	1 Bound // Determined [DIS]
	1 Hide // Seek [DIS]
	*/
	/**
	 * format: 1 x Card Name [Set Abbr]
	 * 
	 * @param monitor
	 * @throws IOException
	 * @throws InvocationTargetException
	 */
	public void runDeckImport(ICoreProgressMonitor monitor) throws IOException {
		lineNum = 0;
		DeckParser parser = new DeckParser(getStream(), this);
		try {
			parser.addPattern(Pattern.compile("\\s*(\\d+)\\s+([^\\[]*[^\\s\\[])(?:\\s*\\[(.*)\\])?"),
					new ICardField[] { MagicCardField.COUNT, MagicCardField.NAME, MagicCardField.EDITION_ABBR, });
			importData.setFields(
					new ICardField[] { MagicCardField.NAME, MagicCardField.COUNT, MagicCardField.SET });
			do {
				lineNum++;
				try {
					MagicCardPhysical card = createDefaultCard();
					card = parser.readLine(card);
					if (card == null)
						break;
					importCard(card);
					monitor.worked(1);
				} catch (IOException e) {
					throw e;
				}
			} while (true);
		} finally {
			parser.close();
		}
	}
}