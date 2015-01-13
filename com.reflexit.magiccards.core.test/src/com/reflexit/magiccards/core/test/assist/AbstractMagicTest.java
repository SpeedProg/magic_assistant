package com.reflexit.magiccards.core.test.assist;

import junit.framework.TestCase;

public class AbstractMagicTest extends TestCase {
	protected String getAboveComment() {
		return getContents(1)[0].toString();
	}

	protected StringBuilder[] getContents(int sections) {
		try {
			return TestFileUtils.getContentsForTest("src", getClass(),
					getName(), sections);
		} catch (Exception e) {
			fail(e.getMessage());
			return null;
		}
	}
}
