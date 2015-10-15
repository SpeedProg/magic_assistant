package com.reflexit.magiccards.core.model.xml;

import gnu.trove.map.TIntFloatMap;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.IDbPriceStore;
import com.reflexit.magiccards.core.seller.CustomPriceProvider;
import com.reflexit.magiccards.core.seller.FindMagicCardsPrices;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.IPriceProviderStore;
import com.reflexit.magiccards.core.seller.ParseMOTLPrices;
import com.reflexit.magiccards.core.seller.ParseMagicCardMarketPrices;
import com.reflexit.magiccards.core.seller.ParseMtgFanaticPrices;
import com.reflexit.magiccards.core.seller.ParseTcgPlayerPrices;
import com.reflexit.magiccards.core.sync.CurrencyConvertor;
import com.reflexit.magiccards.core.xml.PriceProviderStoreObject;

public class DbPricesMultiFileStore implements IDbPriceStore {
	static private DbPricesMultiFileStore instance;
	private IPriceProvider current = null;
	private Set<IPriceProvider> providers = new LinkedHashSet<IPriceProvider>();
	private boolean initialized;

	private void loadPrices(File pricesFile) {
		MagicLogger.traceStart("loadPrices");
		try {
			PriceProviderStoreObject store = PriceProviderStoreObject.initFromFile(pricesFile);
			String name = store.name;
			if (name == null || name.isEmpty())
				name = pricesFile.getName().replace(".xml", "");
			IPriceProvider provider = findProvider(name);
			if (provider == null) {
				provider = new CustomPriceProvider(name);
				add(provider);
			}
			if (store.properties != null)
				provider.getProperties().putAll(store.properties);
			final TIntFloatMap map = provider.getPriceMap();
			if (store.map != null) {
				map.putAll(store.map);
			}
		} catch (IOException e) {
			MagicLogger.log(e);
		} finally {
			MagicLogger.traceEnd("loadPrices");
		}
	}

	@Override
	public void reloadPrices() {
		MagicLogger.traceStart("reloadPrices");
		final IDbCardStore<IMagicCard> db = DataManager.getInstance().getMagicDBStore();
		try {
			db.updateList(null, Collections.singleton(MagicCardField.DBPRICE));
		} finally {
			MagicLogger.traceEnd("reloadPrices");
		}
	}

	public synchronized static IDbPriceStore getInstance() {
		if (instance == null) {
			instance = new DbPricesMultiFileStore();
		}
		return instance;
	}

	private DbPricesMultiFileStore() {
		add(ParseTcgPlayerPrices.create(ParseTcgPlayerPrices.Type.Medium));
		add(ParseTcgPlayerPrices.create(ParseTcgPlayerPrices.Type.Low));
		ParseMtgFanaticPrices mtgFanatic = new ParseMtgFanaticPrices();
		FindMagicCardsPrices findMagicCards = new FindMagicCardsPrices();
		add(ParseMOTLPrices.getInstance());
		add(mtgFanatic);
		add(findMagicCards);
		add(ParseMagicCardMarketPrices.getInstance());
		current = getDefaultProvider();
	}

	private void add(IPriceProvider provider) {
		providers.add(provider);
	}

	private void loadFromXml() {
		File pricesDir = DataManager.getInstance().getPricesDir();
		File[] listFiles = pricesDir.listFiles();
		if (listFiles == null)
			return;
		for (int i = 0; i < listFiles.length; i++) {
			File file = listFiles[i];
			try {
				if (file.getName().endsWith(".xml"))
					loadPrices(file);
			} catch (Exception e) {
				MagicLogger.log(e);
			}
		}
	}

	@Override
	public Collection<IPriceProvider> getProviders() {
		return providers;
	}

	public IPriceProvider getDefaultProvider() {
		return getProviders().iterator().next();
	}

	@Override
	public synchronized IPriceProviderStore setProviderByName(String name) {
		IPriceProvider prov = findProvider(name);
		if (prov == null) {
			prov = new CustomPriceProvider(name);
			add(prov);
		}
		if (current != prov) {
			current = prov;
			if (isInitialized())
				reloadPrices();
		}
		// System.err.println("Provider is set to " + name);
		return current;
	}

	@Override
	public synchronized IPriceProvider getProvider() {
		return current;
	}

	public synchronized IPriceProvider findProvider(String name) {
		if (name == null)
			return null;
		for (Iterator iterator = getProviders().iterator(); iterator.hasNext();) {
			IPriceProvider provider = (IPriceProvider) iterator.next();
			if (provider.getName().equals(name))
				return provider;
		}
		return null;
	}

	@Override
	public synchronized void setDbPrice(IMagicCard card, float price) {
		current.setDbPrice(card, price, CurrencyConvertor.getCurrency());
	}

	@Override
	public synchronized float getDbPrice(IMagicCard card) {
		return current.getDbPrice(card, CurrencyConvertor.getCurrency());
	}

	@Override
	public synchronized void initialize() {
		if (!initialized) {
			try {
				loadFromXml();
				if (current == null)
					current = getDefaultProvider();
				reloadPrices();
			} finally {
				initialized = true;
			}
		}
	}

	@Override
	public TIntFloatMap getPriceMap(IPriceProviderStore provider) {
		initialize();
		return provider.getPriceMap();
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}
}
