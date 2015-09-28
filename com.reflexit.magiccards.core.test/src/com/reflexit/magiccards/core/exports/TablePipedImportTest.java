package com.reflexit.magiccards.core.exports;

import org.junit.Test;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

import static org.junit.Assert.assertNotNull;

public class TablePipedImportTest extends AbstarctImportTest {
	TableImportDelegate tableImport = new TableImportDelegate();

	private void parse() {
		parse(tableImport);
	}

	@Test
	public void test1_N_x_C() {
		MagicLogger.log("start test");
		addLine("NAME|COUNT");
		addLine("Counterspell|2");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
	}

	@Test
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

	@Test
	public void test3_N_x_C() {
		addLine("NAME|SET|COUNT");
		addLine("Counterspell|Fifth Edition|2");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	@Test
	public void test4_N_noC() {
		addLine("NAME|SET");
		addLine("Counterspell|Fifth Edition");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	@Test
	public void test4_IgnoreField() {
		addLine("NAME|x|SET");
		addLine("Counterspell|22|Fifth Edition");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	@Test
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
	@Test
	public void test5_Abbr() {
		addLine("NAME|EDITION_ABBR");
		addLine("Aven Brigadier|ONS");
		parse();
		assertEquals(1, resSize);
		assertEquals("Aven Brigadier", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Onslaught", card1.getSet());
	}

	@Test
	public void test6_Alias() {
		addLine("NAME|EDITION");
		addLine("Counterspell|Fifth Edition");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	@Test
	public void test_N_x_C_bad() {
		addLine("NAME|SET|COUNT");
		addLine("Counterspell|Fifth Edition|2.1");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals("Fifth Edition", card1.getSet());
		assertNotNull(((MagicCardPhysical) card1).getError());
	}

	//	ID|NAME|COST|TYPE|POWER|TOUGHNESS|ORACLE|SET|RARITY|DBPRICE|LANG|RATING|ARTIST|COLLNUM|RULINGS|TEXT|ENID|PROPERTIES|COUNT|PRICE|COMMENT|LOCATION|CUSTOM|OWNERSHIP|SPECIAL|DATE
	//	-39|name 39|{4}|type 39|4|*|bla 39|set 19|Common|1.2256411|Russian|2.39|Elena 39|39a||bla <br> bla 39|0||5|2.1|comment 40|mem||true|foil,c=mint|Sun Jan 11 22:37:54 EST 2015
	@Test
	public void testFull() {
		String lines = getAboveComment();
		addLine(lines);
		parse();
		assertEquals(1, resSize);
		MagicCardPhysical p = (MagicCardPhysical) card1;
		assertEquals(-39, p.getCardId());
		assertEquals("name 39", p.getName());
		assertEquals("set 19", p.getSet());
		assertEquals("Russian", p.getLanguage());
		assertEquals("bla <br> bla 39", p.getText());
		assertEquals(5, p.getCount());
		assertEquals(2.1f, p.getPrice());
		assertEquals("comment 40", p.getComment());
		assertEquals(true, p.isOwn());
		assertEquals("foil,c=mint", p.getSpecial());
	}

	//ID|NAME|COST|TYPE|POWER|TOUGHNESS|ORACLE|SET|RARITY|DBPRICE|LANG|RATING|ARTIST|COLLNUM|RULINGS|TEXT|ENID|PROPERTIES
	//27166|Fire // Ice (Ice)|{1}{U}|Instant|||Tap target permanent.<br>Draw a card.|Apocalypse|Uncommon|0.0||4.488|Franz Vohwinkel|128||Tap target permanent.<br>Draw a card.|0|{PART=Ice, OTHER_PART=Fire, FLIPID=27165, NOUPDATE=true}
	@Test
	public void testParts() {
		String lines = getAboveComment();
		addLine(lines);
		parse();
		assertEquals(1, resSize);
		MagicCardPhysical p = (MagicCardPhysical) card1;
		assertEquals(true, (boolean) p.getBase().getProperty(MagicCardField.NOUPDATE));
		assertEquals("Ice", p.getBase().getPart());
		assertEquals(27165, p.getBase().getFlipId());
	}
}
