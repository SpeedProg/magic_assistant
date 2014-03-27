package com.reflexit.magiccards.core.model.storage;

import gnu.trove.map.TIntFloatMap;
import java.util.Collection;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.seller.IPriceProvider;

public interface IDbPriceStore {
	IPriceProvider setProviderName(String name);

	Collection<IPriceProvider> getProviders();

	IPriceProvider getProvider();

	void initialize();

	TIntFloatMap getPriceMap(IPriceProvider provider);

	float getDbPrice(IMagicCard card);

	void setDbPrice(IMagicCard card, float price);

	void save();

	boolean isInitialized();

	void reloadPrices();
}
