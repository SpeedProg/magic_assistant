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

/**
 * Format example Card Name,Online,For Trade,Physical#,Rarity,Set,No. Words of
 * Wind,1,1,0,R,ONS,122/350 Standardize,1,1,0,R,ONS,116/350 Elvish Vanguard,1,1,0,R,ONS,259/350
 * Gigapede,1,1,0,R,ONS,264/350 Ravenous Baloth,1,1,0,R,ONS,278/350 Biorhythm,1,1,0,R,ONS,247/350
 * Goblin Piledriver,1,1,0,R,ONS,205/350 Tephraderm,1,1,0,R,ONS,239/350 Gratuitous
 * Violence,1,1,0,R,ONS,212/350 Risky Move,1,1,0,R,ONS,223/350 Aven Brigadier,1,1,0,R,ONS,7/350 Aven
 * Brigadier (premium),1,1,0,R,ONS,7/350
 */
public class MtgoImportTest extends AbstarctImportTest {
	private MtgoImportDelegate mtgoImport = new MtgoImportDelegate();

	private void parse() {
		parse(true, mtgoImport);
	}

	public void test1() {
		addLine("Card Name,Online,For Trade,Physical#,Rarity,Set,No.");
		addLine("Aven Brigadier,1,1,0,R,ONS,7/350");
		parse();
		assertEquals(1, resSize);
		assertEquals("Aven Brigadier", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Onslaught", card1.getSet());
	}

	public void test_premium() {
		addLine("Card Name,Online,For Trade,Physical#,Rarity,Set,No.");
		addLine("Aven Brigadier (premium),1,1,0,R,ONS,7/350");
		parse();
		assertEquals(1, resSize);
		assertEquals("Aven Brigadier", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Onslaught", card1.getSet());
	}

	public void test_special() {
		addLine("Aven Brigadier (premium),1,1,0,R,ONS,7/350");
		parse(false, mtgoImport);
		assertEquals(1, resSize);
		assertEquals("Aven Brigadier", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Onslaught", card1.getSet());
		String comment = ((MagicCardPhysical) card1).getSpecial();
		assertTrue(comment, comment.contains("premium"));
	}

	public void testNewFormat() {
		addLine("Card Name,Online,For Trade,Rarity,Set,No.,Premium");
		// Event Ticket,2,0,EVENT, ,No
		addLine("Arrogant Bloodlord,9,5,U,ROE,94/248,No");
		addLine("Arrogant Bloodlord,1,0,U,ROE,94/248,Yes");
		parse();
		assertEquals(2, resSize);
		assertEquals("Arrogant Bloodlord", card1.getName());
		assertEquals(9, ((MagicCardPhysical) card1).getCount());
		assertEquals("Rise of the Eldrazi", card1.getSet());
		assertEquals("premium,", ((MagicCardPhysical) card2).getSpecial());
		// assertEquals("248", ((MagicCardPhysical) card2).getCard().getCollNumber());
	}
}
