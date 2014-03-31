package com.reflexit.magiccards.core.seller;

import gnu.trove.map.TIntFloatMap;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.exports.ClassicNoXExportDelegate;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.IDbPriceStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;

public class AbstractPriceProvider implements IPriceProvider {
	protected String name;
	protected TIntFloatMap priceMap;

	public AbstractPriceProvider(String name) {
		this.name = name;
	}

	public void updatePricesAndSync(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor) throws IOException {
		priceMap = DataManager.getDBPriceStore().getPriceMap(this);
		monitor.beginTask("Loading prices from " + getURL() + " ...", 200);
		try {
			Iterable<IMagicCard> res = updatePrices(iterable, new SubCoreProgressMonitor(monitor, 100));
			DataManager.getDBPriceStore().save();
			sync(res, new SubCoreProgressMonitor(monitor, 100));
		} finally {
			monitor.done();
		}
	}

	public int getSize(Iterable<IMagicCard> iterable) {
		int size = 0;
		for (IMagicCard magicCard : iterable) {
			String set = magicCard.getSet();
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
		if (res != null) {
			IDbCardStore<IMagicCard> db = DataManager.getMagicDBStore();
			TIntFloatMap priceMap = dbPriceStore.getPriceMap(this);
			for (IMagicCard mc : res) {
				IMagicCard base = mc.getBase();
				int id = base.getCardId();
				if (db.getCard(id) != base) {
					float dbprice = priceMap.get(id);
					if (dbprice > 0)
						((MagicCard) base).setDbPrice(dbprice);
				}
			}
		}
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

	protected void setDbPrice(IMagicCard magicCard, float price) {
		int id = magicCard.getCardId();
		int fid = magicCard.getFlipId();
		if (fid != 0) {
			priceMap.put(fid, price);
		}
		priceMap.put(id, price);
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
}
