package com.reflexit.magiccards.core.model;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.magiccards.core.legality.Format;

public class LegalityMapTest extends TestCase {
	private static final Format STANDARD = Format.STANDARD;
	LegalityMap map;

	@Override
	@Before
	public void setUp() throws Exception {
		map = new LegalityMap();
	}

	@Test
	public void testLegalityMapBoolean() {
		map = new LegalityMap(true);
		assertEquals(Legality.UNKNOWN, map.get(STANDARD));
	}

	@Test
	public void testToExternalEmpty() {
		assertEquals("", map.toExternal());
	}

	@Test
	public void testToExternalOne() {
		map.put(STANDARD, Legality.LEGAL);
		assertEquals("Standard+|", map.toExternal());
		roundCheck();
	}

	protected void roundCheck() {
		assertEquals(map, LegalityMap.valueOf(map.toExternal()));
	}

	@Test
	public void testToExternalTwo() {
		map.put(STANDARD, Legality.LEGAL);
		map.put("Bla Bla", Legality.RESTRICTED);
		String expected = "Standard+|Bla Bla1|";
		assertEquals(expected, map.toExternal());
		roundCheck();
	}

	@Test
	public void testGetLabel() {
		map.put(STANDARD, Legality.LEGAL);
		map.put("Extended", Legality.LEGAL);
		assertEquals("Standard+", map.getLabel());
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
	public void testPutStringLegality2() {
		map.put(STANDARD, Legality.NOT_LEGAL);
		map.put(STANDARD, Legality.LEGAL);
		assertEquals(Legality.NOT_LEGAL, map.get(STANDARD));
	}

	/*
	 * @Test public void testFullText() { fail("Not yet implemented"); }
	 * 
	 * @Test public void testCalculateDeckLegality() { fail("Not yet implemented"); }
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
		// System.err.println(map.toExternal());
	}
}
