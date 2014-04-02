package com.reflexit.magiccards.core.seller.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.ParseTcgPlayerPrices;

public class ParseTcgPlayerPricesTest extends AbstractPriceProviderTest {
	@Override
	protected IPriceProvider getPriceProvider() {
		return ParseTcgPlayerPrices.create(ParseTcgPlayerPrices.Type.Medium);
	}

	public void testgetPriceLow() {
		setParser(ParseTcgPlayerPrices.create(ParseTcgPlayerPrices.Type.Low));
		MagicCard card = checkcard("Flameborn Viron", "New Phyrexia");
		assertThat(0, is(not(centPrice(card))));
	}

	public void testgetPriceMed() {
		MagicCard card = checkcard("Flameborn Viron", "New Phyrexia");
		assertThat(0, is(not(centPrice(card))));
	}

	public void testMagic2014() {
		MagicCard card = checkcard("Artificer's Hex", "Magic 2014 Core Set");
		assertThat(0, is(not(centPrice(card))));
	}
}
