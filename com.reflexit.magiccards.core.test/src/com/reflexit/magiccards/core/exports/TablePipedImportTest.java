package com.reflexit.magiccards.core.exports;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class TablePipedImportTest extends AbstarctImportTest {
	TableImportDelegate tableImport = new TableImportDelegate();

	private void parse() {
		parse(true, tableImport);
	}

	public void test1_N_x_C() {
		MagicLogger.log("start test");
		addLine("NAME|COUNT");
		addLine("Counterspell|2");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
	}

	public void test2_N_x_C() {
		addLine("NAME|COUNT");
		addLine("Blust|3");
		addLine("Counterspell|2");
		parse();
		assertEquals(2, resSize);
		assertEquals("Counterspell", card2.getName());
		assertEquals(2, ((MagicCardPhysical) card2).getCount());
		assertEquals(3, ((MagicCardPhysical) card1).getCount());
	}

	public void test3_N_x_C() {
		addLine("NAME|SET|COUNT");
		addLine("Counterspell|Fifth Edition|2");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	public void test4_N_noC() {
		addLine("NAME|SET");
		addLine("Counterspell|Fifth Edition");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	public void test4_IgnoreField() {
		addLine("NAME|x|SET");
		addLine("Counterspell|22|Fifth Edition");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	public void test4_Abbr() {
		addLine("NAME|EDITION_ABBR");
		addLine("Risky Move|ONS");
		parse();
		assertEquals(1, resSize);
		assertEquals("Risky Move", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Onslaught", card1.getSet());
	}

	/*
	 * NAME|EDITION_ABBR Aven Brigadier|ONS
	 */
	public void test5_Abbr() {
		addLine("NAME|EDITION_ABBR");
		addLine("Aven Brigadier|ONS");
		parse();
		assertEquals(1, resSize);
		assertEquals("Aven Brigadier", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Onslaught", card1.getSet());
	}

	public void test6_Alias() {
		addLine("NAME|EDITION");
		addLine("Counterspell|Fifth Edition");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	public void test_N_x_C_bad() {
		addLine("NAME|SET|COUNT");
		addLine("Counterspell|Fifth Edition|2.1");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals("Fifth Edition", card1.getSet());
		assertNotNull(((MagicCardPhysical) card1).getError());
	}
}
