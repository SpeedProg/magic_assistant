package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.IViewPage;

public interface IDeckPage extends IDisposable, IViewPage {
	/**
	 * Sets the deck store for the page
	 */
	public void setFilteredStore(IFilteredCardStore store);
}
