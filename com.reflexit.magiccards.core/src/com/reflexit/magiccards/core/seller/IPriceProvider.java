package com.reflexit.magiccards.core.seller;

import java.net.URL;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public interface IPriceProvider extends IStoreUpdator {
	String getName();

	URL getURL();

	void buy(IFilteredCardStore<IMagicCard> cards);
}
