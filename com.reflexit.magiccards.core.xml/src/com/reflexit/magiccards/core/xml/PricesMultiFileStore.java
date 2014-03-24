package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.IDbPriceStore;
import com.reflexit.magiccards.core.seller.AbstractPriceProvider;
import com.reflexit.magiccards.core.seller.CustomPriceProvider;
import com.reflexit.magiccards.core.seller.FindMagicCardsPrices;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.ParseMOTLPrices;
import com.reflexit.magiccards.core.seller.ParseMtgFanaticPrices;
import com.reflexit.magiccards.core.seller.ParseTcgPlayerPrices;

public class PricesMultiFileStore implements IDbPriceStore {
	static private PricesMultiFileStore instance;
	private IPriceProvider current = null;
	private IPriceProvider loaded = null;

	public synchronized void loadPrices(IPriceProvider provider) {
		MagicLogger.traceStart("reloadPrices");
		File pricesFile = PricesXmlStreamWriter.getPricesFile(provider);
		IDbCardStore<IMagicCard> db = DataManager.getMagicDBStore();
		db.getStorage().setAutoCommit(false);
		try {
			CardCollectionStoreObject store = CardCollectionStoreObject.initFromFile(pricesFile);
			for (IMagicCard base : db) {
				float dbPrice = base.getDbPrice();
				if (dbPrice != 0) {
					((MagicCard) base).setDbPrice(0);
					db.update(base);
				}
			}
			if (store.list != null)
				for (Object o : store.list) {
					IMagicCard card = (IMagicCard) o;
					float dbPrice = card.getDbPrice();
					IMagicCard base = db.getCard(card.getCardId());
					if (base != null) {
						((MagicCard) base).setDbPrice(dbPrice);
						db.update(base);
					}
				}
		} catch (IOException e) {
			MagicLogger.log(e);
		} finally {
			db.getStorage().setAutoCommit(true);
			db.getStorage().save();
			loaded = provider;
			MagicLogger.traceEnd("reloadPrices");
		}
	}

	public static IDbPriceStore getInstance() {
		if (instance == null) {
			instance = new PricesMultiFileStore();
		}
		return instance;
	}

	private Collection<IPriceProvider> providers = new ArrayList<IPriceProvider>();

	private PricesMultiFileStore() {
		ParseMtgFanaticPrices mtgFanatic = new ParseMtgFanaticPrices();
		FindMagicCardsPrices findMagicCards = new FindMagicCardsPrices();
		providers.add(new ParseMOTLPrices());
		providers.add(mtgFanatic);
		providers.add(findMagicCards);
		providers.add(new ParseTcgPlayerPrices(ParseTcgPlayerPrices.Type.Medium));
		providers.add(new ParseTcgPlayerPrices(ParseTcgPlayerPrices.Type.Low));
		loadCustomProviders();
	}

	private void loadCustomProviders() {
		File pricesDir = PricesXmlStreamWriter.getPricesDir();
		File[] listFiles = pricesDir.listFiles();
		if (listFiles == null)
			return;
		for (int i = 0; i < listFiles.length; i++) {
			File file = listFiles[i];
			if (!isCustom(file.getAbsoluteFile()))
				continue;
			loadCustom(file);
		}
	}

	private void loadCustom(File pricesFile) {
		MagicLogger.log("Loading custom provider " + pricesFile);
		try {
			CardCollectionStoreObject store = CardCollectionStoreObject.initFromFile(pricesFile);
			String name = store.name;
			if (name == null)
				name = pricesFile.getName().replace(".xml", "");
			AbstractPriceProvider customPriceProvider = new CustomPriceProvider(name);
			providers.add(customPriceProvider);
			File shouldBeName = PricesXmlStreamWriter.getPricesFile(customPriceProvider);
			if (!shouldBeName.equals(pricesFile))
				pricesFile.renameTo(shouldBeName);
			MagicLogger.log("Custom provider loaded: " + name);
		} catch (IOException e) {
			MagicLogger.log(e);
		}
	}

	private boolean isCustom(File file) {
		for (Iterator iterator = providers.iterator(); iterator.hasNext();) {
			IPriceProvider pp = (IPriceProvider) iterator.next();
			File pricesFile = PricesXmlStreamWriter.getPricesFile(pp);
			if (file.equals(pricesFile))
				return false;
		}
		return true;
	}

	public Collection<IPriceProvider> getProviders() {
		return providers;
	}

	public IPriceProvider getDefaultProvider() {
		return providers.iterator().next();
	}

	public synchronized IPriceProvider setProviderName(String name) {
		IPriceProvider prov = findProvider(name);
		if (prov == null)
			throw new IllegalArgumentException("No such provider");
		current = prov;
		return current;
	}

	public synchronized IPriceProvider getProvider() {
		return current;
	}

	public synchronized IPriceProvider findProvider(String name) {
		if (name == null)
			return null;
		for (Iterator iterator = providers.iterator(); iterator.hasNext();) {
			IPriceProvider provider = (IPriceProvider) iterator.next();
			if (provider.getName().equals(name))
				return provider;
		}
		return null;
	}

	@Override
	public synchronized void initialize() {
		if (current != null && loaded != current)
			loadPrices(current);
	}
}
