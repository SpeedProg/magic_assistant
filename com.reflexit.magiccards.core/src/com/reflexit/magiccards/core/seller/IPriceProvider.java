package com.reflexit.magiccards.core.seller;

import java.io.IOException;
import java.net.URL;
import java.util.Currency;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public interface IPriceProvider extends IPriceProviderStore {
	URL getURL();

	URL buy(Iterable<IMagicCard> cards);

	public String export(Iterable<IMagicCard> cards);

	/**
	 * Update prices for given card list in price storage for actve provider and
	 * dbprice on the list
	 * 
	 * @param iterable
	 *            - if not null - used to get exact card list to update prices
	 *            for
	 * @param monitor
	 *            - progress monitor
	 * @throws IOException
	 */
	public void updatePricesAndSync(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor)
			throws IOException;

	void save() throws IOException;

	void setDbPrice(IMagicCard card, float price, Currency cur);

	float getDbPrice(IMagicCard card, Currency cur);

	Currency getCurrency();
}
