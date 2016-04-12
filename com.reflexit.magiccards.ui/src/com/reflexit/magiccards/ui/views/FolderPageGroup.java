package com.reflexit.magiccards.ui.views;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class FolderPageGroup extends ViewPageGroup {
	private CTabFolder folder;

	public FolderPageGroup(Consumer<IViewPage> beforeActivate, Consumer<IViewPage> afterActivate) {
		super(beforeActivate, afterActivate);
	}

	@Override
	public void createContent(Composite parent) {
		folder = new CTabFolder(parent, SWT.BOTTOM);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		// Pages
		super.createContent(folder);
		// Common
		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateActivePage();
			}
		});
		folder.setSelection(0);
		folder.setSimple(false);
		Display display = folder.getDisplay();
		// folder.setBackground(display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
		folder.setBackground(new Color[] { display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND),
				display.getSystemColor(SWT.COLOR_WHITE) }, new int[] { 50 });
	}

	protected void updateActivePage() {
		CTabItem sel = folder.getSelection();
		if (sel.isDisposed())
			return;
		IViewPage activePage = (IViewPage) sel.getData();
		activate(activePage);
	}

	@Override
	public boolean activate(int page) {
		folder.setSelection(page);
		return super.activate(page);
	}

	@Override
	protected void createPageContent(ViewPageContribution vc, Composite parent) {
		final IViewPage page = vc.getViewPage();
		final CTabItem item = new CTabItem(folder, SWT.CLOSE);
		item.setText(vc.getName());
		item.setShowClose(false);
		page.init(getViewPart());
		page.createContents(folder);
		item.setControl(page.getControl());
		item.setData(page);
	}
}
