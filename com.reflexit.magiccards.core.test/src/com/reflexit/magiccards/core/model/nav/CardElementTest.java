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
		assertEquals("b", CardElement.basename("a/b.c"));
	}

	public void testNameFromFileNoDir() {
		assertEquals("b", CardElement.basename("b.c"));
	}

	public void testNameFromFileNoExt() {
		assertEquals("b", CardElement.basename("b"));
	}

	public void testNameFromFileEmptyExt() {
		assertEquals("b", CardElement.basename("b."));
	}

	public void testNameFromFileOnlyExt() {
		assertEquals("", CardElement.basename(".ext"));
	}

	public void testNameFromFileBasic1() {
		assertEquals("bccc", CardElement.basename("a/bccc.c"));
	}

	public void testNameFromFileOnlyExtDir() {
		assertEquals("", CardElement.basename("c/.ext"));
	}

	public void testNameFromFileWin() {
		if (windows)
		assertEquals("b", CardElement.basename("c:\\dir\\b.ext"));
	}

	public void testNameFromFileWinDouble() {
		if (windows)
		assertEquals("b.ext", CardElement.basename("c:\\dir\\b.ext.2"));
	}
}
