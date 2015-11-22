package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public class LazyTableViewer extends ExtendedTableViewer implements IMagicColumnViewer {
	public LazyTableViewer(Composite parent, ColumnCollection collection) {
		super(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL | SWT.BORDER | SWT.H_SCROLL);
		this.manager = new ViewerManager(collection) {
			@Override
			public
			Viewer getViewer() {
				return LazyTableViewer.this;
			}
		};
		createContents();
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

	@Override
	public void createContents() {
		setContentProvider(new LazyTableViewContentProvider());
		setUseHashlookup(true);
		updatePresentation();
		createDefaultColumns();
	}
	// @Override
	// public void setInput(Object input) {
	// if (this.viewer.getControl().isDisposed())
	// return;
	// updatePresentation();
	// // long time = System.currentTimeMillis();
	// if (this.viewer.getInput() != input) {
	// this.viewer.setInput(input);
	// int size = getContentProvider().getSize(input);
	// this.viewer.setItemCount(size);
	// } else {
	// this.viewer.setSelection(new StructuredSelection());
	// this.viewer.getTable().clearAll();
	// ((ExtendedTableViewer) this.viewer).unmapAllElements();
	// int size = getContentProvider().getSize(input);
	// this.viewer.setItemCount(size);
	// this.viewer.refresh(true);
	// }
	// // System.err.println("set input time: " + (System.currentTimeMillis() -
	// // time) + " ms");
	// }
}
