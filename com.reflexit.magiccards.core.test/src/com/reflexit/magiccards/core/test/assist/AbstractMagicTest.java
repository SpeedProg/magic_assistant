package com.reflexit.magiccards.core.test.assist;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import com.reflexit.unittesting.TestFileUtils;

@FixMethodOrder(MethodSorters.JVM)
public class AbstractMagicTest {
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

	@Rule
	public TestName name = new TestName();

	public String getName() {
		return name.getMethodName();
	}

	public void assertEquals(double a, double b) {
		Assert.assertEquals(a, b, 0.0001);
	}

	public void assertEquals(String a, String b) {
		Assert.assertEquals(a, b);
	}

	public void assertEquals(boolean a, boolean b) {
		Assert.assertEquals(a, b);
	}

	public void assertEquals(int a, int b) {
		Assert.assertEquals(a, b);
	}

	public void assertTrue(boolean equals) {
		Assert.assertTrue(equals);
	}

	public void assertTrue(String s, boolean equals) {
		Assert.assertTrue(s, equals);
	}

	public void fail(String message) {
		Assert.fail(message);
	}

	public void assertEquals(Object a, Object b) {
		Assert.assertEquals(a, b);
	}
}
