package com.reflexit.magiccards.core.exports;

import org.junit.Test;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.unittesting.CardGenerator;

public class CsvExportDelegateTest extends AbstarctExportTest {
	private CsvExportDelegate exporter = new CsvExportDelegate();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		exporter.setReportType(ImportExportFactory.createReportType("test"));
	}

	@Test
	public void test1() {
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue(lines[0].startsWith("ID,NAME"));
	}

	@Test
	public void test2() {
		exporter.setColumns(new ICardField[] { MagicCardField.COUNT, MagicCardField.NAME });
		run(exporter);
		assertEquals(4, lines.length);
		assertTrue("Not good " + lines[0], lines[0].equals("COUNT,NAME"));
		assertTrue("Not good " + lines[0], lines[1].equals(card1.getCount() + "," + card1.getName()));
	}

	@Test
	public void testEscape() {
		exporter.setColumns(new ICardField[] { MagicCardField.COUNT, MagicCardField.NAME });
		card1.set(MagicCardField.NAME, "My,Name");
		run(exporter);
		assertTrue("Does not match " + lines[1], lines[1].startsWith(card1.getCount() + "," + "\"My,Name\""));
	}

	@Test
	public void testMc() {
		card3 = CardGenerator.generateCardWithValues();
		card1 = card2 = null;
		run(exporter);
		assertEquals(2, lines.length);
		assertTrue(lines[0].startsWith("ID,NAME"));
		assertTrue("Does not match " + lines[1],
				lines[1].startsWith(card3.getCardId() + "," + card3.getName()));
	}
}
