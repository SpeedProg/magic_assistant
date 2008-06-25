package com.reflexit.magiccards.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

import com.reflexit.magiccards.core.MagicException;

public interface ICardHandler {
	public IFilteredCardStore getMagicCardHandler();

	public IFilteredCardStore getMagicLibraryHandler();

	public IFilteredCardStore getDeckHandler(String id);

	public int downloadFromUrl(String url, IProgressMonitor pm) throws MagicException, InterruptedException;

	public void loadInitialIfNot(IProgressMonitor nullProgressMonitor) throws MagicException;
}
