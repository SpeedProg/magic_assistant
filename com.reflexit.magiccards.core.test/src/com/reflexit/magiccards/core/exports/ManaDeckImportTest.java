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

import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class ManaDeckImportTest extends AbstarctImportTest {
	/*-
	#DCK#
	UG Madness
	#DECK#
	2 City of Brass
	11 Island
	9 Forest
	4 Wild Mongrel
	4 Basking Rootwalla
	4 Aquamoeba
	4 Arrogant Wurm
	3 Wonder
	3 Roar of the Wurm
	3 Deep Analysis
	4 Circular Logic
	4 Careful Study
	2 Unsummon
	1 Ray of Revelation
	2 Quiet Speculation
	#SIDEBOARD#
	2 Ray of Revelation
	2 Gigapede
	2 Counterspell
	2 Turbulent Dreams
	2 Upheaval
	2 Ravenous Baloth
	2 Merfolk Looter
	1 Krosan Reclamation
	 */
	private ManaDeckDckImportDelegate mimport = new ManaDeckDckImportDelegate();

	private void parse() {
		parse(true, mimport);
	}

	public void test1() {
		addLine("#DCK#\r\n" + "UG Madness\r\n" + "#DECK#\r\n" + "2 City of Brass\r\n" + "11 Island\r\n" + "9 Forest\r\n"
				+ "4 Wild Mongrel\r\n" + "4 Basking Rootwalla\r\n" + "4 Aquamoeba\r\n" + "4 Arrogant Wurm\r\n" + "3 Wonder\r\n"
				+ "3 Roar of the Wurm\r\n" + "3 Deep Analysis\r\n" + "4 Circular Logic\r\n" + "4 Careful Study\r\n" + "2 Unsummon\r\n"
				+ "1 Ray of Revelation\r\n" + "2 Quiet Speculation\r\n" + "#SIDEBOARD#\r\n" + "2 Ray of Revelation\r\n" + "2 Gigapede\r\n"
				+ "2 Counterspell\r\n" + "2 Turbulent Dreams\r\n" + "2 Upheaval\r\n" + "2 Ravenous Baloth\r\n" + "2 Merfolk Looter\r\n"
				+ "1 Krosan Reclamation\n");
		parse();
		assertEquals(23, resSize);
		assertEquals("City of Brass", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
		MagicCardPhysical card = (MagicCardPhysical) result.get(resSize - 1);
		assertTrue(card.isSideboard());
	}
}
