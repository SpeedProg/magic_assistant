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
		this.viewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI | SWT.BORDER);
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

	protected LazyTreeViewContentProvider getContentProvider() {
		return (LazyTreeViewContentProvider) getViewer().getContentProvider();
	}

	@Override
	public void updateViewer(Object input) {
		updateTableHeader();
		updateGrid();
		if (input == null) {
			viewer.setInput(null);
			return;
		}
		synchronized (input) {
			// long time = System.currentTimeMillis();
			// if (this.viewer.getInput() != this.getDataHandler()) {
			if (this.viewer.getInput() != input) {
				this.viewer.setInput(input);
			}
			// System.err.println("set input1 tree time: " +
			// (System.currentTimeMillis() - time) + " ms");
			int size = getContentProvider().getSize(input);
			// System.err.println("size=" + size);
			// MagicLogger.traceStart("treeSet");
			// MagicLogger.trace("size " + size);
			this.viewer.getTree().setItemCount(size);
			try {
				this.viewer.refresh(true);
			} catch (Exception e) {
				this.viewer.setInput(input);
			}
			if (size == 1) {
				// MagicLogger.trace("expand");
				viewer.expandToLevel(2);
			}
			// MagicLogger.traceEnd("treeSet");
		}
		// } else {
		// this.viewer.setSelection(new StructuredSelection());
		// this.viewer.getTree().clearAll(true);
		// ((MyTreeViewer) this.viewer).unmapAllElements();
		// this.viewer.refresh(true);
		// }
		// System.err.println("set input2 tree time: " +
		// (System.currentTimeMillis() - time) + " ms");
	}

	@Override
	public void setSortColumn(int index, int direction) {
		int sortDirection = getSortDirection();
		if (index >= 0) {
			if (direction == -1)
				sortDirection = SWT.DOWN;
			else if (direction == 0)
				sortDirection = SWT.NONE;
			else
				sortDirection = SWT.UP;
		}
		setControlSortColumn(index, sortDirection);
	}
}
