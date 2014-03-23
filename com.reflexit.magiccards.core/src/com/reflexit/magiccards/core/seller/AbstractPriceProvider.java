package com.reflexit.magiccards.core.seller;

import java.io.IOException;
import java.net.URL;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class AbstractPriceProvider implements IPriceProvider {
	protected String name;

	public AbstractPriceProvider(String name) {
		this.name = name;
	}

	@Override
	public void updateStore(ICardStore<IMagicCard> store, Iterable<IMagicCard> iterable, int size, ICoreProgressMonitor monitor)
			throws IOException {
		throw new MagicException("This price provider " + name + " does not support interactive update");
	}

	@Override
	public URL getURL() {
		return null;
	}

	@Override
	public URL buy(IFilteredCardStore<IMagicCard> cards) {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}
}
