package com.reflexit.magiccards.ui.preferences;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.util.IPropertyChangeListener;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.seller.CustomPriceProvider;
import com.reflexit.magiccards.core.seller.FindMagicCardsPrices;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.ParseMOTLPrices;
import com.reflexit.magiccards.core.seller.ParseMtgFanaticPrices;
import com.reflexit.magiccards.core.seller.ParseTcgPlayerPrices;
import com.reflexit.magiccards.core.xml.CardCollectionStoreObject;
import com.reflexit.magiccards.core.xml.PricesXmlStreamWriter;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class PriceProviderManager implements IPropertyChangeListener {
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
			CustomPriceProvider customPriceProvider = new CustomPriceProvider(name);
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

	@Override
	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
		String property = event.getProperty();
		Object newValue = event.getNewValue();
		if (property.equals(PreferenceConstants.PRICE_PROVIDER)) {
			if (newValue != null && !newValue.equals(event.getOldValue())) {
				IPriceProvider provider = findProvider((String) newValue);
				if (provider != null)
					reloadPrices(provider);
			}
		}
	}

	private void reloadPrices(IPriceProvider provider) {
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
			MagicLogger.traceEnd("reloadPrices");
		}
	}
}
