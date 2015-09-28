package com.reflexit.magiccards.core.exports;

import org.junit.Test;

import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class DeckParserTest extends AbstarctImportTest {
	private ClassicImportDelegate classicImport = new ClassicImportDelegate();

	private void parse() {
		super.parse(classicImport);
	}

	@Test
	public void test1_N_x_C() {
		addLine("Counterspell x 2");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
	}

	@Test
	public void test2_N_x_C() {
		addLine("Blust x 3");
		addLine("Counterspell x 2");
		parse();
		assertEquals(2, resSize);
		assertEquals("Counterspell", card2.getName());
		assertEquals(2, ((MagicCardPhysical) card2).getCount());
		assertEquals(3, ((MagicCardPhysical) card1).getCount());
	}

	@Test
	public void test3_N_x_C() {
		addLine("Counterspell (Fifth Edition) x 2");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	@Test
	public void test5_N_x_C() {
		addLine("Myr Matrix x 2");
		parse();
		assertEquals(1, resSize);
		assertEquals("Myr Matrix", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
	}

	@Test
	public void test4_N_x_C() {
		addLine("Blust X 3");
		addLine("Counterspell x2");
		parse();
		assertEquals(2, resSize);
		assertEquals("Counterspell", card2.getName());
		assertEquals(2, ((MagicCardPhysical) card2).getCount());
		assertEquals(3, ((MagicCardPhysical) card1).getCount());
	}

	@Test
	public void test1_C_x_N() {
		addLine("2 x Counterspell (Fifth Edition)");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
		assertEquals("Fifth Edition", card1.getSet());
	}

	@Test
	public void test2_C_x_N() {
		addLine("2 x Counterspell");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(2, ((MagicCardPhysical) card1).getCount());
	}

	@Test
	public void test3_C_x_N() {
		addLine("4x Counterspell");
		parse();
		assertEquals(1, resSize);
		assertEquals("Counterspell", card1.getName());
		assertEquals(4, ((MagicCardPhysical) card1).getCount());
	}
}
