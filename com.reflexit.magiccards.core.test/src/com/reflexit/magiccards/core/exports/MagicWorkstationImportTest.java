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
 * "Name";"Qty";"Rarity";"Edition";"Color";"Cost";"P/T";"Type";"Mana";"Number";"Foil"
 * "Disrupting Scepter";"1";"R";"4E";"Art";"3";"";"Artifact";"3";"316";""
 * "Throne of Bone";"1";"U";"4E";"Art";"1";"";"Artifact";"1";"353";""
 * "Hanabi Blast";"1";"U";"CHK";"R";"1RR";"";"Instant";"3";"170";""
 * "Feral Lightning";"1";"U";"SOK";"R";"3RRR";"";"Sorcery";"6";"101";""
 * "Forest (2)";"1";"C";"8E";"Lnd";"";"";"Basic Land - Forest";"0";"348";""
 */
public class MagicWorkstationImportTest extends AbstarctImportTest {
	// "Disrupting Scepter";"1";"R";"4E";"Art";"3";"";"Artifact";"3";"316";""
	private final MagicWorkstationImportDelegate worker = new MagicWorkstationImportDelegate();

	private void parse() {
		parse(true, worker);
	}

	private void parseLine(String str) {
		addLine(str);
		parse(false, worker);
	}

	public void test1() {
		addLine("\"Name\";\"Qty\";\"Rarity\";\"Edition\";\"Color\";\"Cost\";\"P/T\";\"Type\";\"Mana\";\"Number\";\"Foil\"");
		addLine("\"Disrupting Scepter\";\"1\";\"R\";\"4E\";\"Art\";\"3\";\"\";\"Artifact\";\"3\";\"316\";\"\"");
		parse();
		assertEquals(1, resSize);
		assertEquals("Disrupting Scepter", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fourth Edition", card1.getSet());
	}

	public void test2() {
		parseLine("\"Hanabi Blast\";\"1\";\"U\";\"CHK\";\"R\";\"1RR\";\"\";\"Instant\";\"3\";\"170\";\"\"");
		assertEquals("Champions of Kamigawa", card1.getSet());
	}

	public void test_brackets() {
		parseLine("\"Forest (2)\";\"1\";\"C\";\"8E\";\"Lnd\";\"\";\"\";\"Basic Land - Forest\";\"0\";\"348\";\"\"");
		assertEquals("Land", card1.getRarity());
		assertEquals("Eighth Edition", card1.getSet());
	}
}
