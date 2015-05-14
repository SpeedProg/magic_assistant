package com.reflexit.magiccards.core.model;

import java.io.IOException;
import java.util.Properties;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.IDbPriceStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public interface ICardHandler {
	public IFilteredCardStore getMagicDBFilteredStore();

	public IFilteredCardStore getMagicDBFilteredStoreWorkingCopy();

	public IFilteredCardStore getLibraryFilteredStore();

	public ICardStore getLibraryCardStore();

	public IDbCardStore getMagicDBStore();

	public IFilteredCardStore getLibraryFilteredStoreWorkingCopy();

	public IFilteredCardStore getCardCollectionFilteredStore(String id);

	public ICardStore getActiveStore();

	public String getActiveDeckId();

	public ICardStore loadFromXml(String filename);

	public void setActiveDeckId(String id);

	public int downloadUpdates(String set, Properties options, ICoreProgressMonitor pm)
			throws MagicException, InterruptedException;

	public ICardStore getCardStore(Location to);

	public void loadFromFlatResource(String string) throws IOException;

	public IDbPriceStore getDBPriceStore();
}
