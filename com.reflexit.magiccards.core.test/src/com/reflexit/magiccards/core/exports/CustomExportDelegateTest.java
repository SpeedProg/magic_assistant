package com.reflexit.magiccards.core.exports;

import com.reflexit.magiccards.core.model.MagicCardField;

public class CustomExportDelegateTest extends AbstarctExportTest {
	private CustomExportDelegate exporter = new CustomExportDelegate();
	private ReportType rtype;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		rtype = ReportType.createReportType("test");
		rtype.setCustom(true);
		exporter.setReportType(rtype);
	}

	@Override
	protected void tearDown() throws Exception {
		rtype.delete();
		super.tearDown();
	}

	public void testDefaults() {
		run(exporter);
		assertEquals(3, lines.length);
		assertTrue("Does not match " + lines[0], lines[0].equals(card1.getCount() + " " + card1.getName()));
	}

	public void testDefaultsSep() {
		card1.setObjectByField(MagicCardField.NAME, "My Name");
		rtype.setProperty(CustomExportDelegate.ROW_FORMAT_TYPE_NUM, CustomExportDelegate.FORMAT_SEP_QUOT);
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0].startsWith("COUNT"));
		assertTrue("Does not match " + lines[1], lines[1].startsWith(card1.getCount() + "," + "My Name"));
	}

	public void testSepPipe() {
		card1.setObjectByField(MagicCardField.NAME, "My|Name");
		rtype.setProperty(CustomExportDelegate.ROW_FORMAT_TYPE_NUM, CustomExportDelegate.FORMAT_SEP);
		rtype.setProperty(CustomExportDelegate.ROW_FORMAT, "|");
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0].startsWith("COUNT"));
		assertTrue("Does not match " + lines[1], lines[1].startsWith(card1.getCount() + "|" + "My|Name"));
	}

	public void testSepPipeQuot() {
		card1.setObjectByField(MagicCardField.NAME, "My|Name");
		rtype.setProperty(CustomExportDelegate.ROW_FORMAT_TYPE_NUM, CustomExportDelegate.FORMAT_SEP_QUOT);
		rtype.setProperty(CustomExportDelegate.ROW_FORMAT, "|");
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0].startsWith("COUNT"));
		assertTrue("Does not match " + lines[1], lines[1].startsWith(card1.getCount() + "|" + "\"My|Name\""));
	}

	public void testSepSbField() {
		card1.setLocation(card1.getLocation().toSideboard());
		rtype.setProperty(CustomExportDelegate.ROW_FORMAT_TYPE_NUM, CustomExportDelegate.FORMAT_SEP);
		rtype.setProperty(CustomExportDelegate.ROW_FIELDS, "COUNT,NAME,SIDEBOARD");
		rtype.setProperty(CustomExportDelegate.SB_FIELD, "Yes/No");
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0].startsWith("COUNT"));
		assertTrue("Does not match " + lines[1], lines[1].startsWith(card1.getCount() + "," + card1.getName() + ",Yes"));
	}

	public void testSepSbField2() {
		card1.setLocation(card1.getLocation().toSideboard());
		rtype.setProperty(CustomExportDelegate.ROW_FORMAT_TYPE_NUM, CustomExportDelegate.FORMAT_SEP);
		rtype.setProperty(CustomExportDelegate.ROW_FIELDS, "COUNT,NAME,SIDEBOARD");
		rtype.setProperty(CustomExportDelegate.SB_FIELD, "SB");
		run(exporter);
		assertEquals(4, lines.length);
		assertEquals(card1.getCount() + "," + card1.getName() + ",SB", lines[1]);
		assertEquals(card2.getCount() + "," + card2.getName() + ",", lines[2]);
	}

	public void testSbField() {
		card1.setLocation(card1.getLocation().toSideboard());
		rtype.setProperty(CustomExportDelegate.ROW_FORMAT_TYPE_NUM, CustomExportDelegate.FORMAT_PRINTF);
		rtype.setProperty(CustomExportDelegate.ROW_FIELDS, "SIDEBOARD,COUNT,NAME");
		rtype.setProperty(CustomExportDelegate.SB_FIELD, "SB: ");
		rtype.setProperty(CustomExportDelegate.ROW_FORMAT, "%s%d %s");
		rtype.setProperty(CustomExportDelegate.HEADER, "#start");
		run(exporter);
		assertEquals(4, lines.length);
		assertEquals("SB: " + card1.getCount() + " " + card1.getName(), lines[1]);
		assertEquals(card2.getCount() + " " + card2.getName(), lines[2]);
	}
}
