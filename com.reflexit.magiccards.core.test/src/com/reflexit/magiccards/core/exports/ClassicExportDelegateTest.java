package com.reflexit.magiccards.core.exports;

import com.reflexit.magiccards.core.model.MagicCardField;

public class ClassicExportDelegateTest extends AbstarctExportTest {
	private ClassicExportDelegate exporter = new ClassicExportDelegate();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		exporter.setReportType(ReportType.createReportType("test"));
	}

	public void test1() {
		run(exporter);
		System.err.println(out.toString());
		assertEquals(4, lines.length);
	}

	public void test2() {
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[1].equals(card1.getCount() + "x " + card1.getName()));
	}

	public void testEscape() {
		card1.setObjectByField(MagicCardField.NAME, "My|Name");
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0].startsWith("#"));
		assertTrue("Does not match " + lines[1], lines[1].startsWith(card1.getCount() + "x " + "My|Name"));
	}
}
