/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia. All rights reserved. This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.exports;

import org.junit.Test;

import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

/**
 * Format example Card Name,Online,For Trade,Physical#,Rarity,Set,No. Words of Wind,1,1,0,R,ONS,122/350
 * Standardize,1,1,0,R,ONS,116/350
 * Elvish Vanguard,1,1,0,R,ONS,259/350 Gigapede,1,1,0,R,ONS,264/350 Ravenous Baloth,1,1,0,R,ONS,278/350
 * Biorhythm,1,1,0,R,ONS,247/350 Goblin
 * Piledriver,1,1,0,R,ONS,205/350 Tephraderm,1,1,0,R,ONS,239/350 Gratuitous Violence,1,1,0,R,ONS,212/350 Risky
 * Move,1,1,0,R,ONS,223/350 Aven
 * Brigadier,1,1,0,R,ONS,7/350 Aven Brigadier (premium),1,1,0,R,ONS,7/350
 */
public class MtgoImportTest extends AbstarctImportTest {
	private MtgoImportDelegate mtgoImport = new MtgoImportDelegate();

	private void parse() {
		parse(mtgoImport);
	}

	@Test
	public void test1() {
		addLine("Card Name,Online,For Trade,Physical#,Rarity,Set,No.");
		addLine("Aven Brigadier,1,1,0,R,ONS,7/350");
		parse();
		assertEquals(1, resSize);
		assertEquals("Aven Brigadier", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Onslaught", card1.getSet());
	}

	@Test
	public void test_premium() {
		addLine("Card Name,Online,For Trade,Physical#,Rarity,Set,No.");
		addLine("Aven Brigadier (premium),1,1,0,R,ONS,7/350");
		parse();
		assertEquals(1, resSize);
		assertEquals("Aven Brigadier", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Onslaught", card1.getSet());
	}

	@Test
	public void test_special() {
		addLine("Card Name,Online,For Trade,Physical#,Rarity,Set,No.");
		addLine("Aven Brigadier (premium),1,1,0,R,ONS,7/350");
		parse(mtgoImport);
		assertEquals(1, resSize);
		assertEquals("Aven Brigadier", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Onslaught", card1.getSet());
		String comment = ((IMagicCardPhysical) card1).getSpecial();
		assertTrue(comment, comment.contains("premium"));
	}

	@Test
	public void testNewFormat() {
		addLine("Card Name,Online,For Trade,Rarity,Set,No.,Premium");
		// Event Ticket,2,0,EVENT, ,No
		addLine("Arrogant Bloodlord,9,0,U,ROE,94/248,No");
		addLine("Arrogant Bloodlord,1,0,U,ROE,94/248,Yes");
		parse();
		assertEquals(2, resSize);
		assertEquals("Arrogant Bloodlord", card1.getName());
		assertEquals(9, ((MagicCardPhysical) card1).getCount());
		assertEquals("Rise of the Eldrazi", card1.getSet());
		assertEquals("premium", ((IMagicCardPhysical) card2).getSpecial());
		// assertEquals("248", ((MagicCardPhysical) card2).getCard().getCollNumber());
	}

	/*-
	Card Name,Online,For Trade,Rarity,Set,No.,Premium
	Arrogant Bloodlord,9,5,U,ROE,94/248,Yes
	 */
	@Test
	public void testForTrade() {
		addLine("Card Name,Online,For Trade,Rarity,Set,No.,Premium");
		// Event Ticket,2,0,EVENT, ,No
		addLine("Arrogant Bloodlord,9,5,U,ROE,94/248,Yes");
		parse();
		MagicCardPhysical mcp = (MagicCardPhysical) card1;
		assertEquals("Arrogant Bloodlord", mcp.getName());
		assertEquals("Rise of the Eldrazi", mcp.getSet());
		if (resSize == 1) {
			assertEquals(9, mcp.getCount());
			assertEquals(5, mcp.getForTrade());
			assertEquals(true, mcp.isSpecialTag("premium"));
			MagicCardPhysical ncard = mcp.tradeSplit(mcp.getCount(), mcp.getForTrade());
			assertEquals(4, mcp.getCount());
			assertEquals(0, mcp.getForTrade());
			assertEquals(true, mcp.isSpecialTag("premium"));
			assertEquals(5, ncard.getCount());
			assertEquals(5, ncard.getForTrade());
			assertEquals(true, ncard.isSpecialTag("premium"));
			assertEquals(true, ncard.isForTrade());
		} else {
			MagicCardPhysical ncard = (MagicCardPhysical) card1;
			mcp = (MagicCardPhysical) card2;
			assertEquals(4, mcp.getCount());
			assertEquals(0, mcp.getForTrade());
			assertEquals(true, mcp.isSpecialTag("premium"));
			assertEquals(5, ncard.getCount());
			assertEquals(5, ncard.getForTrade());
			assertEquals(true, ncard.isSpecialTag("premium"));
			assertEquals(true, ncard.isForTrade());
		}
	}

	/*-
	Card Name,Quantity,ID #,Rarity,Set,Collector #,Premium,
	"Crucible of the Spirit Dragon",1,55382,Rare,FRF,167/185,No
	"Akoum Refuge",2,34588,Uncommon,ZEN,210/249,No
	"Bloodfell Caves",4,55552,Common,FRF,165/185,No
	 */
	@Test
	public void testFormat2015() {
		addLine(getAboveComment());
		parse();
		assertEquals(3, resSize);
		assertEquals("Crucible of the Spirit Dragon", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("FRF", card1.getEdition().getMainAbbreviation());
		assertEquals(167, card1.getCollectorNumberId());
		assertEquals("", ((IMagicCardPhysical) card2).getSpecial());
	}
}
