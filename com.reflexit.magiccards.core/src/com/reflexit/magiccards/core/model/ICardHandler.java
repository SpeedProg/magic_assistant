package com.reflexit.magiccards.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

import java.util.Collection;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public interface ICardHandler {
	public IFilteredCardStore getDatabaseHandler();

	public IFilteredCardStore getMyCardsHandler();

	public IFilteredCardStore getCardCollectionHandler(String id);

	public IFilteredCardStore getActiveDeckHandler();

	public ICardStore loadFromXml(String filename);

	public void setActiveDeckHandler(IFilteredCardStore store);

	public boolean copyCards(Collection cards, String to);

	public boolean moveCards(Collection cards, String from, String to);

	public int downloadUpdates(String set, IProgressMonitor pm) throws MagicException, InterruptedException;

	public void loadInitialIfNot(IProgressMonitor nullProgressMonitor) throws MagicException;
}
