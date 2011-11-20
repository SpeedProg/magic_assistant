package com.reflexit.magiccards.core.seller;

import java.io.IOException;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public interface IStoreUpdator {
	/**
	 * Update cards (from iterable) in a given store
	 * 
	 * @param store
	 *            - card store (used to save updates and fire the events)
	 * @param iterable
	 *            - if not null - used to get exact card list, if null store iterator is used
	 * @param size
	 *            - size of the cards (for iterable)
	 * @param monitor
	 *            - progress monitor
	 * @throws IOException
	 */
	public void updateStore(ICardStore<IMagicCard> store, Iterable<IMagicCard> iterable, int size, ICoreProgressMonitor monitor)
			throws IOException;
}
