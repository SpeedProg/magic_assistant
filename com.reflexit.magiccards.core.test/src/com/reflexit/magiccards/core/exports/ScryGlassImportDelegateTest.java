package com.reflexit.magiccards.core.exports;

import org.junit.Test;

import com.reflexit.magiccards.core.model.IMagicCard;

public class ScryGlassImportDelegateTest extends AbstarctImportTest {
	private ScryGlassImportDelegate mimport = new ScryGlassImportDelegate();

	private void parse() {
		parse(mimport);
	}

	private void parseAbove() {
		addLine(getAboveComment());
		parse(mimport);
	}

	// 1 Library of Lat-Nam [6E]
	// 1 Endrek Sahr, Master Breeder [MM2]
	// 3 Zur's Weirding [6E]
	// 1 Garza Zol, Plague Queen [CS]
	// 1 Garza's Assassin [CS]
	// 1 Sek'Kuar, Deathkeeper [CS]
	// 2 Thousand-Year Elixir [C13]
	// 1 Æther Gale [C14]
	// 1 Æther Snap [C14]
	// 1 Blue Sun's Zenith [C15]
	// 1 Overwhelming Stampede [C15]
	// 1 Edric, Spymaster of Trest [CRS]
	// 1 Æther Snap [DS]
	// 1 Bound // Determined [DIS]
	// 1 Hide // Seek [DIS]
	@Test
	public void test() {
		parseAbove();
		assertEquals("Library of Lat-Nam", card1.getName());
		assertEquals("Endrek Sahr, Master Breeder", card2.getName());
		assertEquals("Modern Masters 2015 Edition", card2.getSet());
		assertEquals("Hide // Seek", cardN.getName());
		assertEquals("Dissension", cardN.getSet());
		IMagicCard card9 = result.get(9);
		assertEquals("Commander 2015", card9.getSet());
	}
}
