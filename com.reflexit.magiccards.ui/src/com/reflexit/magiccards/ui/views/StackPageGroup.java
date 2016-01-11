package com.reflexit.magiccards.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class StackPageGroup extends ViewPageGroup {
	private Composite stack;
	private StackLayout layout;

	@Override
	public void createContent(Composite parent) {
		stack = new Composite(parent, SWT.NONE);
		stack.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new StackLayout();
		stack.setLayout(layout);
		// Pages
		super.createContent(stack);
		layout.topControl = getActivePage().getControl();
	}

	@Override
	public void activate() {
		super.activate();
		layout.topControl = getActivePage().getControl();
	}
}
