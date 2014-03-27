package com.reflexit.magiccards.core.seller;

import java.io.IOException;
import java.net.URL;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public interface IPriceProvider {
	String getName();

	URL getURL();

	URL buy(Iterable<IMagicCard> cards);

	public String export(Iterable<IMagicCard> cards);

	/**
	 * Update prices for given card list in price storage for actve provider
	 * 
	 * @param iterable
	 *            - if not null - used to get exact card list to update prices for
	 * @param monitor
	 *            - progress monitor
	 * @throws IOException
	 */
	public void updatePrices(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor) throws IOException;
}
