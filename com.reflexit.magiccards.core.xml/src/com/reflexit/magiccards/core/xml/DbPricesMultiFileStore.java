package com.reflexit.magiccards.core.xml;

import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.procedure.TIntFloatProcedure;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.IDbPriceStore;
import com.reflexit.magiccards.core.seller.CustomPriceProvider;
import com.reflexit.magiccards.core.seller.FindMagicCardsPrices;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.ParseMOTLPrices;
import com.reflexit.magiccards.core.seller.ParseMtgFanaticPrices;
import com.reflexit.magiccards.core.seller.ParseTcgPlayerPrices;

public class DbPricesMultiFileStore implements IDbPriceStore {
	static private DbPricesMultiFileStore instance;
	private IPriceProvider current = null;
	private HashMap<IPriceProvider, TIntFloatHashMap> providers = new HashMap<IPriceProvider, TIntFloatHashMap>();
	private TIntFloatHashMap currentMap;
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
			TIntFloatHashMap map = providers.get(provider);
			if (map == null) {
				map = new TIntFloatHashMap();
				providers.put(provider, map);
			}
			if (store.map != null) {
				final TIntFloatHashMap map1 = map;
				store.map.forEachEntry(new TIntFloatProcedure() {
					@Override
					public boolean execute(int id, float price) {
						map1.put(id, price);
						return true;
					}
				});
			}
		} catch (IOException e) {
			MagicLogger.log(e);
		} finally {
			MagicLogger.traceEnd("loadPrices");
		}
	}

	public void reloadPrices() {
		MagicLogger.traceStart("reloadPrices");
		final IDbCardStore<IMagicCard> db = DataManager.getMagicDBStore();
		try {
			for (IMagicCard card : db) {
				((MagicCard) card).setDbPrice(0);
			}
			if (currentMap != null) {
				currentMap.forEachEntry(new TIntFloatProcedure() {
					@Override
					public boolean execute(int id, float price) {
						// System.err.println(id + " -> " + price);
						IMagicCard base = db.getCard(id);
						if (base != null) {
							MagicCard mc = (MagicCard) base;
							// System.err.println(id + " -> " + mc);
							if (mc.getDbPrice() != price) {
								mc.setDbPrice(price);
								db.update(base); // XXX too many events
							}
						}
						return true;
					}
				});
			}
		} finally {
			MagicLogger.traceEnd("reloadPrices");
		}
	}

	public static IDbPriceStore getInstance() {
		if (instance == null) {
			instance = new DbPricesMultiFileStore();
		}
		return instance;
	}

	private DbPricesMultiFileStore() {
		ParseMtgFanaticPrices mtgFanatic = new ParseMtgFanaticPrices();
		FindMagicCardsPrices findMagicCards = new FindMagicCardsPrices();
		add(new ParseMOTLPrices());
		add(mtgFanatic);
		add(findMagicCards);
		add(new ParseTcgPlayerPrices(ParseTcgPlayerPrices.Type.Medium));
		add(new ParseTcgPlayerPrices(ParseTcgPlayerPrices.Type.Low));
	}

	private void add(IPriceProvider provider) {
		providers.put(provider, null);
	}

	private void loadFromXml() {
		File pricesDir = PricesXmlStreamWriter.getPricesDir();
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

	public Collection<IPriceProvider> getProviders() {
		return providers.keySet();
	}

	public IPriceProvider getDefaultProvider() {
		return getProviders().iterator().next();
	}

	public synchronized IPriceProvider setProviderName(String name) {
		initialize();
		IPriceProvider prov = findProvider(name);
		if (prov == null)
			throw new IllegalArgumentException("No such provider");
		if (current != prov) {
			current = prov;
			currentMap = providers.get(prov);
			reloadPrices();
		}
		// System.err.println("Provider is set to " + name);
		return current;
	}

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

	public synchronized void setDbPrice(IMagicCard card, float price) {
		if (!initialized)
			initialize();
		if (price == 0)
			currentMap.remove(card.getCardId());
		else
			currentMap.put(card.getCardId(), price);
	}

	public float getDbPrice(IMagicCard card) {
		return currentMap.get(card.getCardId());
	}

	@Override
	public synchronized void initialize() {
		if (!initialized) {
			try {
				loadFromXml();
				for (Iterator iterator = getProviders().iterator(); iterator.hasNext();) {
					IPriceProvider provider = (IPriceProvider) iterator.next();
					if (providers.get(provider) == null) {
						providers.put(provider, new TIntFloatHashMap());
					}
				}
				if (current == null)
					current = getDefaultProvider();
				currentMap = providers.get(current);
				reloadPrices();
			} finally {
				initialized = true;
			}
		}
	}

	@Override
	public TIntFloatMap getPriceMap(IPriceProvider provider) {
		initialize();
		return providers.get(provider);
	}

	public void save() {
		try {
			new PricesXmlStreamWriter().save(current);
		} catch (IOException e) {
			MagicLogger.log(e);
		}
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}
}
