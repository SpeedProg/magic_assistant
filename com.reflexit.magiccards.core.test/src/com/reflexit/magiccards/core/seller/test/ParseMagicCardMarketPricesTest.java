package com.reflexit.magiccards.core.seller.test;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.ParseMagicCardMarketPrices;

import static org.junit.Assert.assertNotEquals;

public class ParseMagicCardMarketPricesTest extends AbstractPriceProviderTest {
	@Override
	protected IPriceProvider getPriceProvider() {
		return ParseMagicCardMarketPrices.getInstance();
	}

	public void testSwamp() {
		MagicCard card = checkcard("Swamp", "Magic 2013");
		assertNotEquals(0, centPrice(card));
	}
}
