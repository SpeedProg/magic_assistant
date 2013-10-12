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
		map = new LegalityMap();
	}

	@Test
	public void testLegalityMapBoolean() {
		map = new LegalityMap();
		assertEquals(Legality.UNKNOWN, map.get(STANDARD));
	}

	@Test
	public void testToExternalEmpty() {
		assertEquals("", map.toExternal());
	}

	@Test
	public void testToExternalOne() {
		map.put(STANDARD, Legality.LEGAL);
		assertEquals("Standard", map.toExternal());
		roundCheck();
	}

	protected void roundCheck() {
		assertEquals(map, LegalityMap.valueOf(map.toExternal()));
	}

	@Test
	public void testToExternalTwo() {
		map.put(STANDARD, Legality.LEGAL);
		map.put(BLA_BLA, Legality.RESTRICTED);
		String expected = "Standard|Bla Bla1";
		assertEquals(expected, map.toExternal());
		roundCheck();
	}

	@Test
	public void testGetLabel() {
		map.put(STANDARD, Legality.LEGAL);
		map.put(Format.EXTENDED, Legality.LEGAL);
		assertEquals("Standard", map.getLabel());
	}

	@Test
	public void testGetLabel2() {
		map = new LegalityMap();
		map.put(Format.MODERN, Legality.LEGAL);
		map.put(Format.EXTENDED, Legality.RESTRICTED);
		assertEquals("Extended (1)", map.getLabel());
	}

	@Test
	public void testValueOf() {
		String value = "Bla Bla!";
		map = LegalityMap.valueOf(value);
		assertEquals(Legality.BANNED, map.get("Bla Bla"));
	}

	@Test
	public void testPutStringLegality() {
		map.put(STANDARD, Legality.LEGAL);
		map.put(STANDARD, Legality.NOT_LEGAL);
		assertEquals(Legality.NOT_LEGAL, map.get(STANDARD));
	}

	@Test
	public void testMergeStringLegality() {
		map.merge(STANDARD, Legality.NOT_LEGAL);
		map.merge(STANDARD, Legality.LEGAL);
		assertEquals(Legality.NOT_LEGAL, map.get(STANDARD));
	}

	@Test
	public void testMergeStringLegality2() {
		map.merge(STANDARD, Legality.LEGAL);
		map.merge(STANDARD, Legality.NOT_LEGAL);
		assertEquals(Legality.NOT_LEGAL, map.get(STANDARD));
	}

	@Test
	public void testMergeStringLegality3() {
		assertFalse(map.keySet().contains(Format.STANDARD));
		map.merge(STANDARD, Legality.UNKNOWN);
		assertTrue(map.keySet().contains(Format.STANDARD));
		assertEquals(Legality.UNKNOWN, map.get(STANDARD));
	}

	@Test
	public void testFullText() {
		map.put(Format.EXTENDED, Legality.RESTRICTED);
		map.put(Format.MODERN, Legality.LEGAL);
		map.complete();
		String fullText = map.fullText();
		assertTrue(fullText.contains("Standard - Not Legal"));
		assertTrue(fullText.contains("Legacy - Legal"));
		assertTrue("Cannot find Extended - Restricted in " + fullText, fullText.contains("Extended - Restricted"));
	}

	@Test
	public void testCalculateDeckLegality() {
		LegalityMap card1 = new LegalityMap();
		LegalityMap card2 = new LegalityMap();
		card1.put(STANDARD, Legality.LEGAL);
		card2.put(STANDARD, Legality.BANNED);
		card1.put(BLA_BLA, Legality.LEGAL);
		card1.put(Format.EXTENDED, Legality.RESTRICTED);
		card2.put(Format.EXTENDED, Legality.UNKNOWN);
		ArrayList<LegalityMap> list = new ArrayList<LegalityMap>();
		list.add(card1);
		list.add(card2);
		map = LegalityMap.calculateDeckLegality(list);
		assertEquals(Legality.BANNED, map.get(STANDARD));
		assertEquals(Legality.RESTRICTED, map.get(Format.EXTENDED));
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
		map.put(Format.MODERN, Legality.LEGAL);
		map.complete();
		assertEquals(Legality.NOT_LEGAL, map.get(STANDARD));
		assertEquals(Legality.LEGAL, map.get(Format.LEGACY));
		// System.err.println(map.toExternal());
	}
}
