package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LazyTreeViewerManager extends TreeViewerManager {
	public LazyTreeViewerManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		this.viewer.getTree().setFont(getFont());
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new LazyTreeViewContentProvider());
		// this.viewer.setLabelProvider(new MagicCardLabelProvider());
		this.viewer.setUseHashlookup(true);
		updateGrid();
		// viewer.setSorter(new NameSorter());
		createDefaultColumns();
		return this.viewer.getControl();
	}

	@Override
	protected LazyTreeViewContentProvider getContentProvider() {
		return (LazyTreeViewContentProvider) getViewer().getContentProvider();
	}

	@Override
	public void updateViewer(Object input) {
		updateTableHeader();
		updateGrid();
		// long time = System.currentTimeMillis();
		// if (this.viewer.getInput() != this.getDataHandler()) {
		if (this.viewer.getInput() != input) {
			this.viewer.setInput(input);
		}
		// System.err.println("set input1 tree time: " +
		// (System.currentTimeMillis() - time) + " ms");
		int size = getContentProvider().getSize(input);
		// System.err.println("size=" + size);
		this.viewer.getTree().setItemCount(size);
		this.viewer.refresh(true);
		// } else {
		// this.viewer.setSelection(new StructuredSelection());
		// this.viewer.getTree().clearAll(true);
		// ((MyTreeViewer) this.viewer).unmapAllElements();
		// this.viewer.refresh(true);
		// }
		// System.err.println("set input2 tree time: " +
		// (System.currentTimeMillis() - time) + " ms");
	}
}
