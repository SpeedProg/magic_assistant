package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.Legality;
import com.reflexit.magiccards.core.model.LegalityMap;

public class ParseGathererLegalityTest extends TestCase {
	private ParseGathererLegality parser;

	@Override
	protected void setUp() {
		parser = new ParseGathererLegality();
	}

	public void testLoad() throws IOException {
		LegalityMap map = parser.getCardLegality(193867);
		assertEquals(Legality.BANNED, map.get("Legacy"));
	}

	public void testBalance() throws IOException {
		LegalityMap map = parser.getCardLegality(202501);
		assertEquals(Legality.BANNED, map.get("Legacy"));
		assertEquals(Legality.BANNED, map.get("Commander"));
		assertEquals(Legality.RESTRICTED, map.get("Vintage"));
		assertEquals(Legality.LEGAL, map.get("Freeform"));
		assertEquals(Legality.NOT_LEGAL, map.get("Standard"));
	}

	public void testAngelicWall() throws IOException {
		LegalityMap map = parser.getCardLegality(370789);
		assertEquals(Legality.LEGAL, map.get("Legacy"));
		assertEquals(Legality.LEGAL, map.get("Commander"));
		assertEquals(Legality.LEGAL, map.get("Vintage"));
		assertEquals(Legality.LEGAL, map.get("Freeform"));
		assertEquals(Legality.LEGAL, map.get("Standard"));
	}

	public void testDeck() {
		LegalityMap map1 = new LegalityMap();
		map1.put(Format.STANDARD, Legality.LEGAL);
		map1.put(Format.EXTENDED, Legality.LEGAL);
		map1.complete();
		LegalityMap map2 = new LegalityMap();
		map2.put(Format.EXTENDED, Legality.RESTRICTED);
		map2.put(Format.valueOf("Tribal Wars"), Legality.LEGAL);
		map2.complete();
		Collection<LegalityMap> maps = new ArrayList<LegalityMap>();
		maps.add(map1);
		maps.add(map2);
		LegalityMap deck = LegalityMap.calculateDeckLegality(maps);
		assertEquals(Legality.RESTRICTED, deck.get("Extended"));
		assertEquals(Legality.NOT_LEGAL, deck.get("Tribal Wars"));
	}
}