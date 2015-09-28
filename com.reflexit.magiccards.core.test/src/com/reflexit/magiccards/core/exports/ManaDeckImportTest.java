/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia. All rights reserved. This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.exports;

import org.junit.Test;

import com.reflexit.magiccards.core.model.MagicCardPhysical;

import static org.junit.Assert.assertFalse;

public class ManaDeckImportTest extends AbstarctImportTest {
	private ManaDeckDckImportDelegate mimport = new ManaDeckDckImportDelegate();
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

	@Test
	public void test1() {
		addLine(getAboveComment());
		preview(mimport);
		assertEquals(23, resSize);
		assertEquals("City of Brass", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
		assertFalse(((MagicCardPhysical) card1).isSideboard());
		assertTrue(((MagicCardPhysical) cardN).isSideboard());
		parse();
		assertEquals(23, resSize);
		assertEquals("City of Brass", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
		assertFalse(((MagicCardPhysical) card1).isSideboard());
		assertTrue(((MagicCardPhysical) cardN).isSideboard());
	}

	private void parse() {
		parse(mimport);
	}
}
