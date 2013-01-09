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
import java.util.Iterator;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.test.assist.TestFileUtils;

/**
 */
public class MTGStudioImportTest extends AbstarctImportTest {
	// "Disrupting Scepter";"1";"R";"4E";"Art";"3";"";"Artifact";"3";"316";""
	private final MTGStudioCsvImportDelegate worker = new MTGStudioCsvImportDelegate();

	private void parse() {
		parse(true, worker);
	}

	private void parseLine(String str) {
		addLine("Name,Qty,Edition");
		addLine(str);
		parse(true, worker);
		assertEquals(1, resSize);
	}

	public void test1() {
		addLine("Name,Qty,Edition");
		addLine("\"Disrupting Scepter\",\"1\",\"4E\"");
		parse();
		assertEquals(1, resSize);
		assertEquals("Disrupting Scepter", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fourth Edition", card1.getSet());
	}

	public void test2() {
		parseLine("\"Hanabi Blast\",\"1\",\"CHK\"");
		assertEquals("Champions of Kamigawa", card1.getSet());
	}

	public void test_brackets() {
		parseLine("\"Forest (2)\",\"1\",\"8E\"");
		assertEquals("Land", card1.getRarity());
		assertEquals("Eighth Edition", card1.getSet());
	}

	public void test_big() throws IOException {
		line = TestFileUtils.saveResourceToString("mtgstudio.csv");
		assertNotNull(line);
		parse();
		line = null;
		assertEquals(6345, resSize);
		int err = 0;
		for (Iterator<IMagicCard> iterator = result.iterator(); iterator.hasNext();) {
			MagicCardPhysical c = (MagicCardPhysical) iterator.next();
			if (c.getError() != null) {
				err++;
			}
		}
		assertEquals(2, err);
	}
}
