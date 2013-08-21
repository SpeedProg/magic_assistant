package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;

import com.reflexit.magiccards.core.model.Legality;

public class ParseGathererLegalityTest extends TestCase {
	private ParseGathererLegality parser;

	@Override
	protected void setUp() {
		parser = new ParseGathererLegality();
	}

	public void testLoad() throws IOException {
		Map<String, Legality> map = parser.getCardLegality(193867);
		assertEquals(Legality.BANNED, map.get("Legacy"));
	}

	public void testBalance() throws IOException {
		Map<String, Legality> map = parser.getCardLegality(202501);
		assertEquals(Legality.BANNED, map.get("Legacy"));
		assertEquals(Legality.BANNED, map.get("Commander"));
		assertEquals(Legality.RESTRICTED, map.get("Vintage"));
		assertEquals(Legality.LEGAL, map.get("Freeform"));
		assertEquals(null, map.get("Standard"));
	}

	public void testAngelicWall() throws IOException {
		Map<String, Legality> map = parser.getCardLegality(370789);
		assertEquals(Legality.LEGAL, map.get("Legacy"));
		assertEquals(Legality.LEGAL, map.get("Commander"));
		assertEquals(Legality.LEGAL, map.get("Vintage"));
		assertEquals(Legality.LEGAL, map.get("Freeform"));
		assertEquals(Legality.LEGAL, map.get("Standard"));
	}
}
