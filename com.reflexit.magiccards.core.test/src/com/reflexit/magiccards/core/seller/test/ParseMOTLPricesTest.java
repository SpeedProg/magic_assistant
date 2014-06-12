package com.reflexit.magiccards.core.seller.test;

import static org.junit.Assert.assertNotEquals;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.ParseMOTLPrices;

public class ParseMOTLPricesTest extends AbstractPriceProviderTest {
	@Override
	protected IPriceProvider getPriceProvider() {
		return ParseMOTLPrices.getInstance();
	}

	public void testSwamp() {
		MagicCard card = checkcard("Swamp", "Magic 2013");
		assertNotEquals(0, centPrice(card));
	}
}
