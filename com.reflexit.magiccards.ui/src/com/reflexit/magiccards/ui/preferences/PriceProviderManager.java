package com.reflexit.magiccards.ui.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.seller.FindMagicCardsPrices;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.ParseMOTLPrices;
import com.reflexit.magiccards.core.seller.ParseMtgFanaticPrices;
import com.reflexit.magiccards.core.seller.ParseTcgPlayerPrices;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class PriceProviderManager {
	static private final PriceProviderManager instance = new PriceProviderManager();
	private Collection<IPriceProvider> providers = new ArrayList<IPriceProvider>();

	private PriceProviderManager() {
		ParseMtgFanaticPrices mtgFanatic = new ParseMtgFanaticPrices();
		FindMagicCardsPrices findMagicCards = new FindMagicCardsPrices();
		providers.add(new ParseMOTLPrices());
		providers.add(mtgFanatic);
		providers.add(findMagicCards);
		providers.add(new ParseTcgPlayerPrices(ParseTcgPlayerPrices.Type.Medium));
		providers.add(new ParseTcgPlayerPrices(ParseTcgPlayerPrices.Type.Low));
	}

	public static final PriceProviderManager getInstance() {
		return instance;
	}

	public Collection<IPriceProvider> getProviders() {
		return providers;
	}

	public IPriceProvider getDefaultProvider() {
		return providers.iterator().next();
	}

	public IPriceProvider getProvider() {
		String name = getProviderName();
		IPriceProvider provider = findProvider(name);
		if (provider == null)
			return providers.iterator().next();
		return provider;
	}

	public IPriceProvider findProvider(String name) {
		if (name == null)
			return null;
		for (Iterator iterator = providers.iterator(); iterator.hasNext();) {
			IPriceProvider provider = (IPriceProvider) iterator.next();
			if (provider.getName().equals(name))
				return provider;
		}
		return null;
	}

	public String getProviderName() {
		String name = MagicUIActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.PRICE_PROVIDER);
		return name;
	}

	public void setProviderName(String name) {
		if (findProvider(name) == null)
			throw new IllegalArgumentException("No such provider");
		MagicUIActivator.getDefault().getPreferenceStore().setValue(PreferenceConstants.PRICE_PROVIDER, name);
	}
}
