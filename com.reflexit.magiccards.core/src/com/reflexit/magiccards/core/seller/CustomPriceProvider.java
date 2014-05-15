package com.reflexit.magiccards.core.seller;

import java.io.IOException;
import java.util.Currency;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.CurrencyConvertor;

public class CustomPriceProvider extends AbstractPriceProvider {
	private Currency cur = CurrencyConvertor.USD;

	public CustomPriceProvider(String name) {
		this(name, null);
	}

	public CustomPriceProvider(String name, String scur) {
		super(name);
		if (scur != null) {
			this.cur = Currency.getInstance(scur);
		}
	}

	@Override
	public Iterable<IMagicCard> updatePrices(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor)
			throws IOException {
		throw new MagicException("This custom price provider " + name + " does not support interactive update");
	}

	@Override
	public void save() throws IOException {
		// no save
	}

	@Override
	public java.util.Currency getCurrency() {
		return cur;
	}
}
