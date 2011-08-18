package com.reflexit.magiccards.ui.graphics;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.lib.AbstractDeckPage;

public class GraphicsDeckPage extends AbstractDeckPage {
	private DesktopCanvas panel;

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		panel = new DesktopCanvas(getArea());
		panel.setLayoutData(new GridData(GridData.FILL_BOTH));
		return getArea();
	}

	@Override
	public void setFilteredStore(IFilteredCardStore store) {
		panel.setInput(store);
		panel.forceFocus();
	}

	@Override
	public String getStatusMessage() {
		return "This page is under contruction...";
	}

	@Override
	public void activate() {
		super.activate();
		// ?
	}
}
