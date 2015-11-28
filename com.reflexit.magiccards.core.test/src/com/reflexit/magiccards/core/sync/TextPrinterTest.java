package com.reflexit.magiccards.core.sync;

import org.junit.Test;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.test.assist.AbstractMagicTest;
import com.reflexit.unittesting.CardGenerator;

public class TextPrinterTest extends AbstractMagicTest {
	@Test
	public void testMCHeader() {
		String string = TextPrinter.getHeader();
		assertEquals(
				"ID|NAME|COST|TYPE|POWER|TOUGHNESS|ORACLE|SET|RARITY|ARTIST|COLLNUM|TEXT|PROPERTIES",
				string);
	}

	@Test
	public void testMC() {
		MagicCard card = CardGenerator.genMagicCard(39);
		card.setRating(2);
		String string = TextPrinter.getString(card);
		assertEquals("-39|name 39|{4}|type 39|4|*|bla 39|set 19|Common|Elena 39|39a|bla <br> bla 39|",
				string);
	}

	@Test
	public void testMCImageURL() {
		MagicCard card = CardGenerator.genMagicCard(39);
		card.set(MagicCardField.IMAGE_URL, "some");
		String string = TextPrinter.getString(card);
		assertEquals(
				"-39|name 39|{4}|type 39|4|*|bla 39|set 19|Common|Elena 39|39a|bla <br> bla 39|{IMAGE_URL=some}",
				string);
	}
}
