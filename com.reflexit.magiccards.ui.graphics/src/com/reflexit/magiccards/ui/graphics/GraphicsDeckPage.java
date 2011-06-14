package com.reflexit.magiccards.ui.graphics;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.lib.AbstractDeckPage;

public class GraphicsDeckPage extends AbstractDeckPage {
	private DeskCanvas panel;

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		getArea().setLayout(new GridLayout());
		panel = new DeskCanvas(getArea());
		panel.setLayoutData(new GridData(GridData.FILL_BOTH));
		return getArea();
	}

	@Override
	public void setFilteredStore(IFilteredCardStore store) {
		panel.setInput(store);
	}

	@Override
	public void updateFromStore() {
		if (store == null)
			return;
		// ?
	}
}
