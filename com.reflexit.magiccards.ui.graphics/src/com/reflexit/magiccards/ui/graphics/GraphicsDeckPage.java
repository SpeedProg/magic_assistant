package com.reflexit.magiccards.ui.graphics;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.lib.AbstractDeckPage;

public class GraphicsDeckPage extends AbstractDeckPage {
	private DesktopCanvas panel;
	private Label status;
	private IFilteredCardStore fstore;

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		status = createStatusLine(getArea());
		panel = new DesktopCanvas(getArea());
		panel.setLayoutData(new GridData(GridData.FILL_BOTH));
		return getArea();
	}

	@Override
	public void setFilteredStore(IFilteredCardStore store) {
		this.fstore = store;
	}

	@Override
	public String getStatusMessage() {
		return "This page is under contruction...";
	}

	@Override
	public void activate() {
		super.activate();
		panel.setInput(fstore);
		panel.forceFocus();
		status.setText(getStatusMessage());
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager toolBarManager) {
		super.fillLocalToolBar(toolBarManager);
		toolBarManager.add(view.getGroupAction());
	}
}
