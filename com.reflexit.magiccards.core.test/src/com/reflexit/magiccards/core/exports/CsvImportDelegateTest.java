package com.reflexit.magiccards.core.exports;

import java.lang.reflect.InvocationTargetException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

@FixMethodOrder(MethodSorters.JVM)
public class CsvImportDelegateTest extends AbstarctImportTest {
	private CsvImportDelegate importd = new CsvImportDelegate();

	private void parseAbove() {
		addLine(getAboveComment());
		parse(true, importd);
	}

	//
	@Test
	public void testEmpty() {
		parseAbove();
		assertEquals(0, resSize);
	}

	//NAME,COUNT
	@Test
	public void testHeaderOnly() {
		parseAbove();
		assertEquals(0, resSize);
	}

	//NAME,COUNT
	//
	@Test
	public void testBlank() {
		parseAbove();
		assertEquals(0, resSize);
	}

	//	ID,NAME,COST,TYPE,POWER,TOUGHNESS,ORACLE,SET,RARITY,DBPRICE,LANG,RATING,ARTIST,COLLNUM,RULINGS,TEXT,ENID,PROPERTIES,COUNT,PRICE,COMMENT,LOCATION,CUSTOM,OWNERSHIP,SPECIAL,DATE
	//	-39,name 39,{4},type 39,4,*,bla 39,set 19,Common,1.2256411,Russian,2.39,Elena 39,39a,,bla <br> bla 39,0,,5,2.1,comment 40,mem,,true,"foil,c=mint",Sun Jan 11 22:37:54 EST 2015
	@Test
	public void testLines() {
		parseAbove();
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

	// NAME,EDITION,QTY
	// Counterspell,Fifth Edition,3
	@Test
	public void test_Alias() {
		parseAbove();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(3, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	// NAME,TEXT
	// Bla,"Test this exciting ""stuff"""
	@Test
	public void test_Escapes() {
		parseAbove();
		assertEquals(1, resSize);
		assertEquals("Bla", card1.getName());
		assertEquals("Test this exciting \"stuff\"", card1.getText());
	}

	// NAME,COUNT
	//
	// Bla,1
	@Test
	public void test_Black1() {
		parseAbove();
		assertEquals(1, resSize);
		assertEquals("Bla", card1.getName());
	}

	// NAME
	// Bla
	@Test(expected = MagicException.class)
	public void test_Inval() throws InvocationTargetException, InterruptedException {
		addLine(getAboveComment());
		parseonly(true, importd);
	}

	// Name,Qty
	// Accursed Spirit,1
	@Test(expected = MagicException.class)
	public void test_InvalHeader() throws InvocationTargetException, InterruptedException {
		addLine(getAboveComment());
		parseonly(true, importd);
	}

	// NAME,QTY,EDITION_ABBR
	// Accursed Spirit,1,M15
	@Test
	public void test_abbr() {
		parseAbove();
		assertEquals(1, resSize);
		assertEquals("Accursed Spirit", card1.getName());
		assertEquals("Magic 2015 Core Set", card1.getSet());
	}

	// NAME,QTY,EDITION_ABBR
	// Accursed Spirit,1,M15W
	@Test
	public void test_abbr_ukn_resolve() {
		parseAbove();
		assertEquals(1, resSize);
		assertEquals("Accursed Spirit", card1.getName());
		assertEquals("Magic 2015 Core Set", card1.getSet());
	}

	// NAME,QTY,EDITION_ABBR
	// Hoo,1,M15W
	@Test
	public void test_abbr_ukn_unknown() {
		parseAbove();
		assertEquals(1, resSize);
		assertEquals("Hoo", card1.getName());
		assertEquals("M15W", card1.getSet());
		assertEquals("Name not found in db", String.valueOf(((MagicCardPhysical) card1).getError()));
	}

	// NAME,COUNT,LOCATION,SIDEBOARD
	//Accursed Spirit,1,deck,true
	//Accursed Spirit,1,deck,
	@Test
	public void test_ignoreLoc() {
		parseAbove();
		assertEquals(2, resSize);
		assertEquals("Accursed Spirit", card1.getName());
		assertEquals("mem-sideboard", ((MagicCardPhysical) card1).getLocation().toString());
		assertEquals("mem", ((MagicCardPhysical) card2).getLocation().toString());
	}

	// NAME,COUNT,FORTRADECOUNT
	//Accursed Spirit,4,1
	@Test
	public void testForTrade() {
		parseAbove();
		assertEquals(2, resSize);
		assertEquals(1, ((MagicCardPhysical) card1).getCount());
		assertEquals(1, ((MagicCardPhysical) card1).getForTrade());
		assertEquals(3, ((MagicCardPhysical) card2).getCount());
		assertEquals(0, ((MagicCardPhysical) card2).getForTrade());
	}
}
