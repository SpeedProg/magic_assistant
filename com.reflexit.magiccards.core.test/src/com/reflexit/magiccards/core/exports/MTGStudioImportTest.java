/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia. All rights reserved. This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.exports;

import java.io.IOException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.unittesting.TestFileUtils;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.JVM)
public class MTGStudioImportTest extends AbstarctImportTest {
	// "Disrupting Scepter";"1";"R";"4E";"Art";"3";"";"Artifact";"3";"316";""
	private final MTGStudioCsvImportDelegate worker = new MTGStudioCsvImportDelegate();

	private void parse() {
		parse(worker);
	}

	private void parseLine(String str) {
		addLine("Name,Qty,Edition");
		addLine(str);
		parse(worker);
		assertEquals(1, resSize);
	}

	@Test
	public void test1() {
		addLine("Name,Qty,Edition");
		addLine("\"Disrupting Scepter\",\"1\",\"4E\"");
		parse();
		assertEquals(1, resSize);
		assertEquals("Disrupting Scepter", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fourth Edition", card1.getSet());
	}

	@Test
	public void test2() {
		parseLine("\"Hanabi Blast\",\"1\",\"CHK\"");
		assertEquals("Champions of Kamigawa", card1.getSet());
	}

	@Test
	public void test_brackets() {
		parseLine("\"Forest (2)\",\"1\",\"8E\"");
		assertEquals("Land", card1.getRarity());
		assertEquals("Eighth Edition", card1.getSet());
	}

	@Test
	public void testTSP() {
		addLine("Name,Edition,Qty");
		addLine("Pendelhaven,TSP,1");
		parse();
		assertEquals("Time Spiral \"Timeshifted\"", card1.getSet());
		assertNull(((MagicCardPhysical) card1).getError());
	}

	@Test
	public void testPR() {
		addLine("Name,Qty,Edition");
		addLine("Sewers of Estark [Harper Prism],2,PR");
		addLine("\"Glissa, the Traitor [Pre 30 Jan 2011 Foil]\",1,PR");
		parse();
		assertEquals(2, resSize);
		assertEquals("Promo set for Gatherer", card1.getSet());
		assertEquals("Promo set for Gatherer", card2.getSet());
		assertNull(((MagicCardPhysical) card1).getError());
	}

	@Test
	public void testDouble() {
		parseLine("Life/Death,2,DDJ");
		assertEquals("Duel Decks: Izzet vs. Golgari", card1.getSet());
		assertNull(((MagicCardPhysical) card1).getError());
		assertEquals("Life // Death (Death)", card1.getName());
	}

	@Test
	public void test_big() throws IOException {
		line = TestFileUtils.saveResourceToString("mtgstudio.csv", getClass());
		assertNotNull(line);
		parse();
		line = null;
		assertEquals(6345, resSize);
		int err = 0;
		for (IMagicCard iMagicCard : result) {
			MagicCardPhysical c = (MagicCardPhysical) iMagicCard;
			if (c.getError() != null) {
				//System.err.println(c.getError() + " " + c);
				err++;
			}
		}
		assertEquals(2, err);
	}
}
