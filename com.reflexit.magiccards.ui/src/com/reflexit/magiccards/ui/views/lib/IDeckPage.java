package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public interface IDeckPage {
	public void setFilteredStore(IFilteredCardStore store);

	public void updateFromStore();

	public Control getControl();

	public String getStatusMessage();
}
