package com.reflexit.magiccards.core.seller.test;

import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.ParseMOTLPrices;

public class ParseMOTLPricesTest extends AbstractPriceProviderTest {
	@Override
	protected IPriceProvider getPriceProvider() {
		return new ParseMOTLPrices();
	}
}
