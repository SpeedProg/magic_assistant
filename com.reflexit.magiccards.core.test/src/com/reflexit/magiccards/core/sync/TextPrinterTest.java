package com.reflexit.magiccards.core.sync;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.test.assist.AbstractMagicTest;
import com.reflexit.magiccards.core.test.assist.CardGenerator;

public class TextPrinterTest extends AbstractMagicTest {
	@Override
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testMCP() {
		MagicCardPhysical phi = new MagicCardPhysical(new MagicCard(), null);
		phi.getBase().setCardId(-39);
		phi.getBase().setName("name 39");
		phi.getBase().setSet("set 39");
		phi.getBase().setRating(0);
		phi.setCount(5);
		phi.setSpecial("foil,c=mint");
		phi.setComment("comment 40");
		phi.setOwn(true);
		phi.setPrice(2.1f);
		phi.setDbPrice(0);
		phi.setDate("Sun Jan 11 22:37:54 EST 2015");
		String string = TextPrinter.toString(phi);
		assertEquals("-39|name 39||||||set 39||0.0||0.0|||||0||5|2.1|comment 40|||true|foil,c=mint|Sun Jan 11 22:37:54 EST 2015",
				string);
	}
	@Test
	public void testMCHeader() {
		String string = TextPrinter.getHeaderMC();
		assertEquals("ID|NAME|COST|TYPE|POWER|TOUGHNESS|ORACLE|SET|RARITY|ARTIST|COLLNUM|TEXT|PROPERTIES",
				string);
	}

	@Test
	public void testMC() {
		MagicCard card = CardGenerator.genMagicCard(39);
		card.setRating(2);
		String string = TextPrinter.toString(card);
		assertEquals("-39|name 39|{4}|type 39|4|*|bla 39|set 19|Common|Elena 39|39a|bla <br> bla 39|",
				string);
	}
}
