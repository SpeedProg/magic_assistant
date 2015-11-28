package com.reflexit.magiccards.core.exports;

import org.junit.Test;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.unittesting.CardGenerator;

public class PipedTableExportText extends AbstarctExportTest {
	private TableExportDelegate exporter = new TableExportDelegate();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		exporter.setReportType(ImportExportFactory.createReportType("test"));
	}

	@Test
	public void test1() {
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0], lines[0].startsWith("ID|NAME"));
	}

	@Test
	public void test2() {
		exporter.setColumns(new ICardField[] { MagicCardField.COUNT, MagicCardField.NAME });
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0], lines[0].equals("COUNT|NAME"));
		assertTrue(lines[1].equals(card1.getCount() + "|" + card1.getName()));
	}

	@Test
	public void testEscape() {
		exporter.setColumns(new ICardField[] { MagicCardField.COUNT, MagicCardField.NAME });
		card1.set(MagicCardField.NAME, "My|Name");
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0].startsWith("COUNT|NAME"));
		assertTrue("Does not match " + lines[1], lines[1].startsWith(card1.getCount() + "|" + "My?Name"));
	}

	@Test
	public void testFull() {
		card1.getBase().setNonEmptyFromCard(CardGenerator.genMagicCard(39));
		card1.setCount(5);
		card1.setSpecial("foil,c=mint");
		card1.setComment("comment 40");
		card1.setOwn(true);
		card1.setPrice(2.1f);
		card1.setDate("Sun Jan 11 22:37:54 EST 2015");
		run(exporter);
		assertEquals(4, lines.length);
		System.err.println(lines[0]);
		System.err.println(lines[1]);
		assertEquals(
				"ID|NAME|COST|TYPE|POWER|TOUGHNESS|ORACLE|SET|RARITY|DBPRICE|LANG|RATING|ARTIST|COLLNUM|RULINGS|TEXT|ENID|PROPERTIES|COUNT|PRICE|COMMENT|LOCATION|CUSTOM|OWNERSHIP|SPECIAL|DATE",
				lines[0]);
		assertEquals(
				"-39|name 39|{4}|type 39|4|*|bla 39|set 19|Common|1.2256411|Russian|2.39|Elena 39|39a||bla <br> bla 39|0||5|2.1|comment 40|mem||true|foil,c=mint|Sun Jan 11 22:37:54 EST 2015",
				lines[1]);
	}
}
