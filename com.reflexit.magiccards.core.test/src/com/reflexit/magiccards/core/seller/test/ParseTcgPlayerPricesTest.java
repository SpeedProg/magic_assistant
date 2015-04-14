package com.reflexit.magiccards.core.seller.test;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.ParseTcgPlayerPrices;

import static org.junit.Assert.assertNotEquals;

public class ParseTcgPlayerPricesTest extends AbstractPriceProviderTest {
	@Override
	protected IPriceProvider getPriceProvider() {
		return ParseTcgPlayerPrices.create(ParseTcgPlayerPrices.Type.Medium);
	}

	public void testgetPriceLow() {
		setParser(ParseTcgPlayerPrices.create(ParseTcgPlayerPrices.Type.Low));
		MagicCard card = checkcard("Flameborn Viron", "New Phyrexia");
		assertNotEquals(0, centPrice(card));
	}

	public void testgetPriceMed() {
		MagicCard card = checkcard("Flameborn Viron", "New Phyrexia");
		assertNotEquals(0, centPrice(card));
	}

	public void testMagic2014() {
		MagicCard card = checkcard("Artificer's Hex", "Magic 2014 Core Set");
		assertNotEquals(0, centPrice(card));
	}

	@Override
	public void testBulkTenthEdition() {
		super.testBulkTenthEdition();
	}
}
