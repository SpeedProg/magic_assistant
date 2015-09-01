package com.reflexit.magiccards.core.seller;

import gnu.trove.map.TIntFloatMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.WebUtils;
import com.reflexit.magiccards.core.xml.PriceProviderStoreObject;
import com.reflexit.magiccards.core.xml.PricesXmlStreamReader;

public class CustomPriceProvider extends AbstractPriceProvider {
	public CustomPriceProvider(String name) {
		super(name);
	}

	@Override
	public Iterable<IMagicCard> updatePrices(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor)
			throws IOException {
		URL url = getURL();
		if (url == null)
			throw new MagicException("This price provider " + name + " does not support interactive update");
		monitor.beginTask("Loading prices from " + url + " ...", 100);
		try {
			if (updateFromWeb(url))
				return iterable;
			else
				return null;
		} finally {
			monitor.done();
		}
	}

	public boolean updateFromWeb(URL url) throws IOException {
		try (InputStream openStream = WebUtils.openUrl(url)) {
			loadPrices(openStream);
		}
		return true;
	}

	private void loadPrices(InputStream st) {
		MagicLogger.traceStart("loadPrices");
		try {
			PriceProviderStoreObject store = new PricesXmlStreamReader().load(st);
			if (store.properties != null)
				getProperties().putAll(store.properties);
			final TIntFloatMap map = getPriceMap();
			if (store.map != null) {
				map.clear();
				map.putAll(store.map);
			}
		} catch (IOException e) {
			MagicLogger.log(e);
		} finally {
			MagicLogger.traceEnd("loadPrices");
		}
	}

	@Override
	public URL getURL() {
		String url = getProperties().getProperty("url");
		try {
			if (url != null)
				return new URL(url);
		} catch (MalformedURLException e) {
			MagicLogger.log(url);
		}
		return null;
	}

	@Override
	public void save() throws IOException {
		if (getURL() != null)
			super.save();
	}
}
