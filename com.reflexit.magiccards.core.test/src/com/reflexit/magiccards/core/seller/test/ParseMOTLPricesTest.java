package com.reflexit.magiccards.core.seller.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

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
		assertThat(0, is(not(centPrice(card))));
	}
}
