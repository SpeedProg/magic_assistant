package com.reflexit.magiccards.core.model;

import java.util.Collection;
import java.util.Properties;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public interface ICardHandler {
	public IFilteredCardStore getMagicDBFilteredStore();

	public IFilteredCardStore getMagicDBFilteredStoreWorkingCopy();

	public IFilteredCardStore getLibraryFilteredStore();

	public ICardStore getLibraryCardStore();

	public IFilteredCardStore getLibraryFilteredStoreWorkingCopy();

	public IFilteredCardStore getCardCollectionFilteredStore(String id);

	public IFilteredCardStore getActiveDeckHandler();

	public ICardStore loadFromXml(String filename);

	public void setActiveDeckHandler(IFilteredCardStore store);

	public boolean copyCards(Collection cards, Location to);

	public boolean moveCards(Collection cards, Location from, Location to);

	public int downloadUpdates(String set, Properties options, ICoreProgressMonitor pm) throws MagicException, InterruptedException;

	public void loadInitialIfNot(ICoreProgressMonitor nullProgressMonitor) throws MagicException;

	public ICardStore getMagicDBStore();
}
