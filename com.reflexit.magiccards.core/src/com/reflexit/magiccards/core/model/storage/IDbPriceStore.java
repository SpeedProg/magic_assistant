package com.reflexit.magiccards.core.model.storage;

import java.util.Collection;

import com.reflexit.magiccards.core.seller.IPriceProvider;

public interface IDbPriceStore {
	void loadPrices(IPriceProvider provider);

	IPriceProvider setProviderName(String name);

	Collection<IPriceProvider> getProviders();

	IPriceProvider getProvider();

	void initialize();
}
