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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Import for classic text deck format
 */
public class ClassicImportDelegate extends AbstractImportDelegate {
	public ClassicImportDelegate() {
	}

	/**
	 * @param monitor
	 * @throws IOException
	 */
	@Override
	public void doRun(ICoreProgressMonitor monitor) throws IOException {
		runDeckImport(monitor);
	}

	/**
	 * format: Card Name (Set) x4 4 x Card Name (Set)
	 * 
	 * @param monitor
	 * @throws IOException
	 * @throws InvocationTargetException
	 */
	public void runDeckImport(ICoreProgressMonitor monitor) throws IOException {
		DeckParser parser = new DeckParser(getStream(), this);
		parser.addPattern(Pattern.compile("\\s*(.*?)\\s*(?:\\(([^)]*)\\))?\\s+[xX]?\\s*(\\d+)\\s*$"), new ICardField[] {
				MagicCardField.NAME, MagicCardField.SET, MagicCardFieldPhysical.COUNT });
		parser.addPattern(Pattern.compile("\\s*(\\d+)\\s*[xX]?\\s+([^(]*[^\\s(])(?:\\s*\\(([^)]*)\\))?"), new ICardField[] {
				MagicCardFieldPhysical.COUNT, MagicCardField.NAME, MagicCardField.SET, });
		importResult.setFields(new ICardField[] { MagicCardField.NAME, MagicCardFieldPhysical.COUNT, MagicCardField.SET });
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
		parser.close();
	}
}
