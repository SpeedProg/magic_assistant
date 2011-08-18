package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public interface IDeckPage {
	/**
	 * Create page contents (composite). Method getControl() should return same
	 * composite as this method returns.
	 */
	public Composite createContents(Composite parent);

	/**
	 * Sets the deck store for the page
	 */
	public void setFilteredStore(IFilteredCardStore store);

	/**
	 * Sets parent deck view
	 */
	public void setDeckView(DeckView view);

	/**
	 * Method is called when page is activate and store is set
	 */
	public void activate();

	/**
	 * Return main page control (created by createComposite method)
	 */
	public Control getControl();

	/**
	 * This will be status message displayed at the top of the page
	 */
	public String getStatusMessage();
}
