package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Legality;
import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

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
		assertEquals(Legality.NOT_LEGAL, map.get("Standard"));
	}

	public void testDeck() {
		LegalityMap map1 = LegalityMap.EMPTY
				.put(Format.STANDARD, Legality.LEGAL)
				.put(Format.MODERN, Legality.LEGAL);
		LegalityMap map2 = LegalityMap.EMPTY
				.put(Format.MODERN, Legality.RESTRICTED)
				.put(Format.valueOf("Tribal Wars"), Legality.LEGAL);
		Collection<LegalityMap> maps = new ArrayList<LegalityMap>();
		maps.add(map1);
		maps.add(map2);
		LegalityMap deck = LegalityMap.calculateDeckLegality(maps);
		assertEquals(Legality.RESTRICTED, deck.get("Modern"));
		assertEquals(Legality.NOT_LEGAL, deck.get("Tribal Wars"));
	}
	

	@Test
	public void testLegalitiesInet() {
		ParseSetLegality.loadAllFormats(ICoreProgressMonitor.NONE);
		Editions editions = Editions.getInstance();
		Edition ed = editions.getEditionByName("Fate Reforged");
		assertEquals(Legality.LEGAL, ed.getLegalityMap().get(Format.STANDARD));
	}
}
