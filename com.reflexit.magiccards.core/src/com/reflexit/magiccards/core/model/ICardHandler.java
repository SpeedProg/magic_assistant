package com.reflexit.magiccards.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

import java.util.Collection;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public interface ICardHandler {
	public IFilteredCardStore getDatabaseHandler();

	public IFilteredCardStore getMyCardsHandler();

	public IFilteredCardStore getDeckHandler(String id);

	public IFilteredCardStore getActiveDeckHandler();

	public void setActiveDeckHandler(IFilteredCardStore store);

	public boolean copyCards(Collection cards, String to);

	public boolean moveCards(Collection cards, String from, String to);

	public int downloadFromUrl(String url, IProgressMonitor pm) throws MagicException, InterruptedException;

	public void loadInitialIfNot(IProgressMonitor nullProgressMonitor) throws MagicException;
}
