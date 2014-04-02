package com.reflexit.magiccards.core.model.storage;

import gnu.trove.map.TIntFloatMap;

import java.util.Collection;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.IPriceProviderStore;

public interface IDbPriceStore {
	IPriceProviderStore setProviderByName(String name);

	Collection<IPriceProvider> getProviders();

	IPriceProvider getProvider();

	void initialize();

	TIntFloatMap getPriceMap(IPriceProviderStore provider);

	float getDbPrice(IMagicCard card);

	void setDbPrice(IMagicCard card, float price);

	boolean isInitialized();

	void reloadPrices();
}
