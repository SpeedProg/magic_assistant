package com.reflexit.magiccards.core.exports;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;

public class PipedTableExportText extends AbstarctExportTest {
	private TableExportDelegate exporter = new TableExportDelegate();

	public void test1() {
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0].startsWith("ID|NAME"));
	}

	public void test2() {
		exporter.setColumns(new ICardField[] { MagicCardFieldPhysical.COUNT, MagicCardField.NAME });
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0], lines[0].equals("COUNT|NAME"));
		assertTrue(lines[1].equals(card1.getCount() + "|" + card1.getName()));
	}

	public void testEscape() {
		exporter.setColumns(new ICardField[] { MagicCardFieldPhysical.COUNT, MagicCardField.NAME });
		card1.setObjectByField(MagicCardField.NAME, "My|Name");
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0].startsWith("COUNT|NAME"));
		assertTrue("Does not match " + lines[1], lines[1].startsWith(card1.getCount() + "|" + "My?Name"));
	}
}
