package com.reflexit.magiccards.core.model.nav;

import java.util.Locale;

import junit.framework.TestCase;

public class CardElementTest extends TestCase {
	private boolean windows;
	
	@Override
	protected void setUp() throws Exception {
		windows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
		super.setUp();
	}

	public void testNameFromFileBasic() {
		assertEquals("b", CardElement.nameFromFile("a/b.c"));
	}

	public void testNameFromFileNoDir() {
		assertEquals("b", CardElement.nameFromFile("b.c"));
	}

	public void testNameFromFileNoExt() {
		assertEquals("b", CardElement.nameFromFile("b"));
	}

	public void testNameFromFileEmptyExt() {
		assertEquals("b", CardElement.nameFromFile("b."));
	}

	public void testNameFromFileOnlyExt() {
		assertEquals("", CardElement.nameFromFile(".ext"));
	}

	public void testNameFromFileBasic1() {
		assertEquals("bccc", CardElement.nameFromFile("a/bccc.c"));
	}

	public void testNameFromFileOnlyExtDir() {
		assertEquals("", CardElement.nameFromFile("c/.ext"));
	}

	public void testNameFromFileWin() {
		if (windows)
		assertEquals("b", CardElement.nameFromFile("c:\\dir\\b.ext"));
	}

	public void testNameFromFileWinDouble() {
		if (windows)
		assertEquals("b.ext", CardElement.nameFromFile("c:\\dir\\b.ext.2"));
	}
}
