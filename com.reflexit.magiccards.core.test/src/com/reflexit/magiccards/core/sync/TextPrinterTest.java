package com.reflexit.magiccards.core.sync;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.test.assist.AbstractMagicTest;

public class TextPrinterTest extends AbstractMagicTest {
	@Override
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		MagicCardPhysical phi = new MagicCardPhysical(new MagicCard(), null);
		phi.getBase().setCardId(-39);
		phi.getBase().setName("name 39");
		phi.getBase().setSet("set 39");
		phi.setCount(5);
		phi.setSpecial("foil,c=mint");
		phi.setComment("comment 40");
		phi.setOwn(true);
		phi.setPrice(2.1f);
		phi.setDate("Sun Jan 11 22:37:54 EST 2015");
		String string = TextPrinter.toString(phi);
		assertEquals("-39|name 39||||||set 39||0.0||0.0|||||0||5|2.1|comment 40|||true|foil,c=mint|Sun Jan 11 22:37:54 EST 2015",
				string);
	}
}
