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
		return new ParseTcgPlayerPrices(ParseTcgPlayerPrices.Type.Medium);
	}

	public void xtestgetPriceHigh() {
		parser = new ParseTcgPlayerPrices(ParseTcgPlayerPrices.Type.High);
		MagicCard card = addcard("Flameborn Viron", "New Phyrexia");
		assertThat(0, is(not(centPrice(card))));
	}

	public void testgetPriceLow() {
		parser = new ParseTcgPlayerPrices(ParseTcgPlayerPrices.Type.Low);
		MagicCard card = addcard("Flameborn Viron", "New Phyrexia");
		assertThat(0, is(not(centPrice(card))));
	}
}
