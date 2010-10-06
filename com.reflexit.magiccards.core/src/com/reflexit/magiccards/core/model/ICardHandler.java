package com.reflexit.magiccards.core.model;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

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

	public int downloadUpdates(String set, Properties options, IProgressMonitor pm) throws MagicException, InterruptedException;

	public void loadInitialIfNot(IProgressMonitor nullProgressMonitor) throws MagicException;
}
