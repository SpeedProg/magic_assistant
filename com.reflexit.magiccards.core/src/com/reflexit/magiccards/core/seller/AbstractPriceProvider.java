package com.reflexit.magiccards.core.seller;

import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.hash.TIntFloatHashMap;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.exports.ClassicNoXExportDelegate;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IDbPriceStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;
import com.reflexit.magiccards.core.xml.PricesXmlStreamWriter;

public class AbstractPriceProvider implements IPriceProvider {
	protected String name;
	protected final TIntFloatMap priceMap;
	protected final Properties properties;

	public AbstractPriceProvider(String name) {
		this.name = name;
		this.properties = new Properties();
		this.priceMap = new TIntFloatHashMap();
	}

	public void updatePricesAndSync(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor) throws IOException {
		monitor.beginTask("Loading prices from " + getURL() + " ...", 200);
		try {
			Iterable<IMagicCard> res = updatePrices(iterable, new SubCoreProgressMonitor(monitor, 100));
			if (res != null) {
				save();
				sync(res, new SubCoreProgressMonitor(monitor, 100));
			}
		} finally {
			monitor.done();
		}
	}

	public int getSize(Iterable<IMagicCard> iterable) {
		int size = 0;
		for (IMagicCard magicCard : iterable) {
			size++;
		}
		return size;
	}

	public Set<String> getSets(Iterable<IMagicCard> iterable) {
		HashSet<String> sets = new HashSet();
		for (IMagicCard magicCard : iterable) {
			String set = magicCard.getSet();
			sets.add(set);
		}
		return sets;
	}

	public void sync(Iterable<IMagicCard> res, ICoreProgressMonitor monitor) {
		IDbPriceStore dbPriceStore = DataManager.getDBPriceStore();
		if (dbPriceStore.getProvider().equals(this))
			dbPriceStore.reloadPrices();
	}

	public Iterable<IMagicCard> updatePrices(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor) throws IOException {
		throw new MagicException("This price provider " + name + " does not support interactive update");
	}

	@Override
	public URL getURL() {
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public URL buy(Iterable<IMagicCard> cards) {
		return null;
	}

	public String export(Iterable<IMagicCard> cards) {
		String res = new ClassicNoXExportDelegate().export(cards);
		return res;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	public synchronized void setDbPrice(IMagicCard magicCard, float price) {
		int id = magicCard.getCardId();
		int fid = magicCard.getFlipId();
		if (fid != 0) {
			setDbPrice(fid, price);
		}
		setDbPrice(id, price);
	}

	public synchronized void setDbPrice(int id, float price) {
		if (id == 0)
			return;
		if (price == 0)
			priceMap.remove(id);
		else
			priceMap.put(id, price);
	}

	public float getDbPrice(IMagicCard card) {
		return priceMap.get(card.getCardId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractPriceProvider other = (AbstractPriceProvider) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public static transient PricesXmlStreamWriter writer = new PricesXmlStreamWriter();

	public void save() throws IOException {
		writer.write(this);
	}

	@Override
	public TIntFloatMap getPriceMap() {
		return priceMap;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}
}
