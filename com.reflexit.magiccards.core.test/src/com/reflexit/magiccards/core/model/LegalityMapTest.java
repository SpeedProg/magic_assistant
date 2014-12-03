package com.reflexit.magiccards.core.model;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.magiccards.core.legality.Format;

public class LegalityMapTest extends TestCase {
	private static final Format STANDARD = Format.STANDARD;
	private static final Format BLA_BLA = Format.valueOf("Bla Bla");
	LegalityMap map;

	@Override
	@Before
	public void setUp() {
		map = LegalityMap.EMPTY;
	}

	@Test
	public void testLegalityMapBoolean() {
		map = LegalityMap.EMPTY;
		assertEquals(Legality.UNKNOWN, map.get(STANDARD));
	}

	@Test
	public void testToExternalEmpty() {
		assertEquals("", map.toExternal());
	}

	@Test
	public void testToExternalOne() {
		map = map.put(STANDARD, Legality.LEGAL);
		assertEquals("Standard", map.toExternal());
		roundCheck();
	}

	protected void roundCheck() {
		assertEquals(map, LegalityMap.valueOf(map.toExternal()));
	}

	@Test
	public void testToExternalTwo() {
		map = map.put(STANDARD, Legality.LEGAL);
		map = map.put(BLA_BLA, Legality.RESTRICTED);
		String expected = "Standard|Bla Bla1";
		assertEquals(expected, map.toExternal());
		roundCheck();
	}

	@Test
	public void testGetLabel() {
		map = map.put(STANDARD, Legality.LEGAL);
		map = map.put(Format.MODERN, Legality.LEGAL);
		assertEquals("Standard", map.getLabel());
	}

	@Test
	public void testGetLabel2() {
		map = LegalityMap.EMPTY;
		map = map.put(Format.MODERN, Legality.LEGAL);
		map = map.put(Format.STANDARD, Legality.RESTRICTED);
		assertEquals("Standard (1)", map.getLabel());
	}

	@Test
	public void testValueOf() {
		String value = "Bla Bla!";
		map = LegalityMap.valueOf(value);
		assertEquals(Legality.BANNED, map.get("Bla Bla"));
	}

	public void testValueOfExtended() {
		map = LegalityMap.valueOf("Extended");
		assertEquals(Legality.LEGAL, map.get(Format.MODERN));
	}

	public void testValueOfExtended1() {
		map = LegalityMap.valueOf("Extended1");
		assertEquals(Legality.RESTRICTED, map.get(Format.MODERN));
	}

	public void testValueOfOther() {
		map = LegalityMap.valueOf("Other");
		Format format = map.mapOfLegality().keySet().iterator().next();
		assertEquals("Standard", format.name());
		assertEquals(Legality.LEGAL, map.get("Other"));
		map = LegalityMap.valueOf("Other1");
		assertEquals(Legality.RESTRICTED, map.get("Other"));
	}

	public void testValueOfWild() {
		map = LegalityMap.valueOf("*");
		assertEquals(Legality.LEGAL, map.get(Format.LEGACY));
	}

	@Test
	public void testPutStringLegality() {
		map = map.put(STANDARD, Legality.LEGAL);
		map = map.put(STANDARD, Legality.NOT_LEGAL);
		assertEquals(Legality.NOT_LEGAL, map.get(STANDARD));
	}

	@Test
	public void testMergeStringLegality() {
		map = map.merge(STANDARD, Legality.NOT_LEGAL);
		map = map.merge(STANDARD, Legality.LEGAL);
		assertEquals(Legality.NOT_LEGAL, map.get(STANDARD));
	}

	@Test
	public void testMergeStringLegality2() {
		map = map.merge(STANDARD, Legality.LEGAL);
		map = map.merge(STANDARD, Legality.NOT_LEGAL);
		assertEquals(Legality.NOT_LEGAL, map.get(STANDARD));
	}

	@Test
	public void testMergeStringLegality3() {
		map = map.put(STANDARD, Legality.UNKNOWN);
		assertEquals(Legality.UNKNOWN, map.get(STANDARD));
	}

	@Test
	public void testMergeMap() {
		map = map.put(STANDARD, Legality.LEGAL);
		LegalityMap map2 = LegalityMap.EMPTY;
		map2 = map2.merge(Format.MODERN, Legality.LEGAL);
		map2 = map2.merge(Format.STANDARD, Legality.NOT_LEGAL);
		map = map.merge(map2);
		assertEquals(Legality.NOT_LEGAL, map.get(STANDARD));
		assertEquals(Legality.LEGAL, map.get(Format.MODERN));
	}

	@Test
	public void testFullText() {
		map = map.put(Format.MODERN, Legality.RESTRICTED);
		map = map.put(Format.LEGACY, Legality.LEGAL).complete();
		String fullText = map.fullText();
		assertTrue(fullText.contains("Standard - Not Legal"));
		assertTrue(fullText.contains("Legacy - Legal"));
		assertTrue("Cannot find Extended - Restricted in " + fullText, fullText.contains("Modern - Restricted"));
	}

	@Test
	public void testCalculateDeckLegality() {
		LegalityMap card1 = LegalityMap.EMPTY;
		LegalityMap card2 = LegalityMap.EMPTY;
		card1 = card1.put(STANDARD, Legality.LEGAL);
		card2 = card2.put(STANDARD, Legality.BANNED);
		card1 = card1.put(Format.MODERN, Legality.RESTRICTED);
		card2 = card2.put(Format.MODERN, Legality.LEGAL);
		card1 = card1.put(BLA_BLA, Legality.LEGAL);
		ArrayList<LegalityMap> list = new ArrayList<LegalityMap>();
		list.add(card1);
		list.add(card2);
		map = LegalityMap.calculateDeckLegality(list);
		assertEquals(Legality.BANNED, map.get(STANDARD));
		assertEquals(Legality.RESTRICTED, map.get(Format.MODERN));
		assertEquals(Legality.NOT_LEGAL, map.get(BLA_BLA));
	}

	/*
	 * 
	 * @Test public void testLegalFormats() { fail("Not yet implemented"); }
	 * 
	 * @Test public void testCompareTo() { fail("Not yet implemented"); }
	 * 
	 * @Test public void testOrdinal() { fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetFirstLegal() { fail("Not yet implemented"); }
	 * 
	 * @Test public void testGetLegality() { fail("Not yet implemented"); }
	 */
	@Test
	public void testComplete() {
		map = map.put(Format.MODERN, Legality.LEGAL).complete();
		assertEquals(Legality.NOT_LEGAL, map.get(STANDARD));
		assertEquals(Legality.LEGAL, map.get(Format.LEGACY));
		// System.err.println(map.toExternal());
	}
}
