package com.reflexit.magiccards.ui.views;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class StackPageGroup extends ViewPageGroup {
	private Composite stack;
	private StackLayout layout;

	public StackPageGroup(Consumer<IViewPage> beforeActivate, Consumer<IViewPage> afterActivate) {
		super(beforeActivate, afterActivate);
	}

	@Override
	public void createContent(Composite parent) {
		stack = new Composite(parent, SWT.NONE);
		stack.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new StackLayout();
		stack.setLayout(layout);
		// stack.setBackground(stack.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		// Pages
		super.createContent(stack);
		setActivePageIndex(0);
		layout.topControl = getActivePage().getControl();
		stack.layout(true, true);
	}

	@Override
	public void activate() {
		layout.topControl = getActivePage().getControl();
		super.activate();
		stack.layout(true, true);
	}
}
