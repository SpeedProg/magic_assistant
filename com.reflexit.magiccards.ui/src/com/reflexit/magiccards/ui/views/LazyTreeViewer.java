package com.reflexit.magiccards.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.model.LazyTreeViewContentProvider;

public class LazyTreeViewer extends ExtendedTreeViewer implements IMagicColumnViewer {
	public LazyTreeViewer(Composite parent, ColumnCollection collection) {
		super(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL | SWT.BORDER | SWT.H_SCROLL);
		setColumnCollection(collection);
	}

	@Override
	protected void createContents() {
		super.createContents();
		setContentProvider(new LazyTreeViewContentProvider());
	}

	@Override
	protected void inputChanged(Object input, Object oldInput) {
		updatePresentation();
		if (input == null) {
			return;
		}
		if (getTree().getItemCount() == 1) {
			// MagicLogger.trace("expand");
			expandToLevel(2);
		}
		// MagicLogger.traceEnd("treeSet");
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
