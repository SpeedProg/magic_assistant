package com.reflexit.magiccards.core.seller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.Platform;

import com.reflexit.magiccards.core.Activator;

public class PriceProviderManager {
	static public final String PROVIDER_PROP = "priceProvider";
	static private final PriceProviderManager instance = new PriceProviderManager();
	private Collection<IPriceProvider> providers = new ArrayList<IPriceProvider>();

	private PriceProviderManager() {
		ParseMtgFanaticPrices mtgFanatic = new ParseMtgFanaticPrices();
		FindMagicCardsPrices findMagicCards = new FindMagicCardsPrices();
		providers.add(new ParseMOTLPrices());
		providers.add(mtgFanatic);
		providers.add(findMagicCards);
		Activator.getDefault().getEclipseDefaultPreferences().put(PROVIDER_PROP, getDefaultProvider().getName());
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
		String name = Platform.getPreferencesService().getString(Activator.PLUGIN_ID, PROVIDER_PROP, null, null);
		return name;
	}

	public void setProviderName(String name) {
		if (findProvider(name) == null)
			throw new IllegalArgumentException("No such provider");
		Activator.getDefault().getEclipsePreferences().put(PROVIDER_PROP, name);
	}
}
