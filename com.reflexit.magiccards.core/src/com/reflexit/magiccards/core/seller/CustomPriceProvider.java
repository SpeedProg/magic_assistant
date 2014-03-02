package com.reflexit.magiccards.core.seller;

import java.io.IOException;
import java.net.URL;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class CustomPriceProvider implements IPriceProvider {
	private String name;

	public CustomPriceProvider(String name) {
		this.name = name;
	}

	@Override
	public void updateStore(ICardStore<IMagicCard> store, Iterable<IMagicCard> iterable, int size, ICoreProgressMonitor monitor)
			throws IOException {
		throw new MagicException("This custom price provider " + name + " does not support interactive update");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public URL getURL() {
		return null;
	}
}
