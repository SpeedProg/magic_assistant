package com.reflexit.magiccards.core.exports;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.reflexit.magiccards.core.model.MagicCardField;

@FixMethodOrder(MethodSorters.JVM)
public class ClassicExportDelegateTest extends AbstarctExportTest {
	private ClassicExportDelegate exporter = new ClassicExportDelegate();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		exporter.setReportType(ImportExportFactory.createReportType("test"));
	}

	@Test
	public void test1() {
		run(exporter);
		System.err.println(out.toString());
		assertEquals(4, lines.length);
	}

	@Test
	public void test2() {
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[1].equals(card1.getCount() + "x " + card1.getName()));
	}

	@Test
	public void testEscape() {
		card1.set(MagicCardField.NAME, "My|Name");
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0].startsWith("#"));
		assertTrue("Does not match " + lines[1], lines[1].startsWith(card1.getCount() + "x " + "My|Name"));
	}
}
