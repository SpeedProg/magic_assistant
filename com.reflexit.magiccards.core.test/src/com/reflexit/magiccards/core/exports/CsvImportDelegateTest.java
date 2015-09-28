package com.reflexit.magiccards.core.exports;

import java.lang.reflect.InvocationTargetException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

import static org.junit.Assert.assertNull;

@FixMethodOrder(MethodSorters.JVM)
public class CsvImportDelegateTest extends AbstarctImportTest {
	private CsvImportDelegate importd = new CsvImportDelegate();

	private void parseAbove() {
		addLine(getAboveComment());
		parse(importd);
	}

	private void previewAbove() {
		addLine(getAboveComment());
		preview(importd);
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
		parseonly(importd);
	}

	// Name,Qty
	// Accursed Spirit,1
	@Test(expected = MagicException.class)
	public void test_InvalHeader() throws InvocationTargetException, InterruptedException {
		addLine(getAboveComment());
		parseonly(importd);
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

	/*-
	NAME,ID,COST,TYPE,POWER,TOUGHNESS,ORACLE,SET,RARITY,CTYPE,COUNT,LOCATION,OWNERSHIP,COMMENT,PRICE,COLOR,DBPRICE,RATING,ARTIST,COLLNUM,SPECIAL,FORTRADECOUNT,LANG,TEXT,OWN_COUNT,OWN_UNIQUE,LEGALITY,SIDEBOARD,ERROR,DATE,SET_RELEASE
	Blighted Agent,214383,{1}{U},Creature - Human Rogue,1,1,Infect <i>(This creature deals damage to creatures in the form of -1/-1 counters and to players in the form of poison counters.)</i><br>Blighted Agent is unblockable.,New Phyrexia,Common,mono,1,Collections/main,true,,0.0,{1}{U},0.0,0.0,,29,,0,,Infect <i>(This creature deals damage to creatures in the form of -1/-1 counters and to players in the form of poison counters.)</i><br>Blighted Agent is unblockable.,1,1,Extended|Commander-,false,,Wed Feb 11 19:27:06 EST 2015,Sun May 01 00:00:00 EDT 2011
	Blind Zealot,217999,{1}{B}{B},Creature - Human Cleric,2,2,"Intimidate <i>(This creature can't be blocked except by artifact creatures and/or creatures that share a color with it.)</i><br>Whenever Blind Zealot deals combat damage to a player, you may sacrifice it. If you do, destroy target creature that player controls.",New Phyrexia,Common,mono,1,Collections/main,true,,0.0,{1}{B}{B},0.0,0.0,,52,,0,,"Intimidate <i>(This creature can't be blocked except by artifact creatures and/or creatures that share a color with it.)</i><br>Whenever Blind Zealot deals combat damage to a player, you may sacrifice it. If you do, destroy target creature that player controls.",1,1,Extended|Commander-,false,,Wed Feb 11 19:27:06 EST 2015,Sun May 01 00:00:00 EDT 2011
	Birthing Pod,218006,{3}{GP},Artifact,,,"<i>({GP} can be paid with either {G} or 2 life.)</i><br>{1}{GP}, {T}, Sacrifice a creature: Search your library for a creature card with converted mana cost equal to 1 plus the sacrificed creature's converted mana cost, put that card onto the battlefield, then shuffle your library. Activate this ability only any time you could cast a sorcery.",New Phyrexia,Rare,mono,1,Collections/main,true,,0.0,{3}{GP},0.0,0.0,,104,,0,,"<i>({GP} can be paid with either {G} or 2 life.)</i><br>{1}{GP}, {T}, Sacrifice a creature: Search your library for a creature card with converted mana cost equal to 1 plus the sacrificed creature's converted mana cost, put that card onto the battlefield, then shuffle your library. Activate this ability only any time you could cast a sorcery.",1,1,Extended|Commander-,false,,Wed Feb 11 19:27:06 EST 2015,Sun May 01 00:00:00 EDT 2011
	Blinding Souleater,233045,{3},Artifact Creature - Cleric,1,3,"{WP}, {T}: Tap target creature. <i>({WP} can be paid with either {W} or 2 life.)</i>",New Phyrexia,Common,colorless,1,Collections/main,true,,0.0,{3},0.0,0.0,,131,,0,,"{WP}, {T}: Tap target creature. <i>({WP} can be paid with either {W} or 2 life.)</i>",1,1,Extended|Commander-,false,,Wed Feb 11 19:27:06 EST 2015,Sun May 01 00:00:00 EDT 2011
	Blade Splicer,233068,{2}{W},Creature - Human Artificer,1,1,"When Blade Splicer enters the battlefield, put a 3/3 colorless Golem artifact creature token onto the battlefield.<br>Golem creatures you control have first strike.",New Phyrexia,Rare,mono,1,Collections/main,true,,0.0,{2}{W},0.0,0.0,,4,,0,,"When Blade Splicer enters the battlefield, put a 3/3 colorless Golem artifact creature token onto the battlefield.<br>Golem creatures you control have first strike.",1,1,Extended|Commander-,false,,Wed Feb 11 19:27:06 EST 2015,Sun May 01 00:00:00 EDT 2011
	*/
	@Test
	public void testMA1_3_1_14() {
		parseAbove();
		assertEquals(5, resSize);
		assertNull(((MagicCardPhysical) card1).getError());
	}

	/*-
	 NAME,SET,LEGALITY,IMAGE_URL
	 My Card,My New Set,Weird,http://bla
	 */
	@Test
	public void testLegality() {
		previewAbove();
		assertEquals(1, resSize);
		assertEquals("http://bla", card1.getBase().getImageUrl());
		assertEquals("Weird", card1.getBase().getLegalityMap().toExternal());
	}

	/*-
	 NAME,SET,LEGALITY,IMAGE_URL
	 Blighted Agent,N Set,Weird,http://bla
	 */
	@Test
	public void testRegression() {
		previewAbove();
		assertEquals(1, resSize);
		assertEquals("Blighted Agent", card1.getName());
		assertEquals("http://bla", card1.getBase().getImageUrl());
		assertEquals("Weird", card1.getBase().getLegalityMap().toExternal());
	}
}
