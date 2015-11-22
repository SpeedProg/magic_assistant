package com.reflexit.magiccards.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public class LazyTreeViewer extends ExtendedTreeViewer implements IMagicColumnViewer {
	public LazyTreeViewer(Composite parent, ColumnCollection collection) {
		super(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL | SWT.BORDER | SWT.H_SCROLL);
		this.manager = new LazyTreeViewerManager(this, collection);
	}

	@Override
	protected void inputChanged(Object input, Object oldInput) {
		manager.updatePresentation();
		if (input == null) {
			return;
		}
		if (getTree().getItemCount() == 1) {
			// MagicLogger.trace("expand");
			expandToLevel(2);
		}
		// MagicLogger.traceEnd("treeSet");
	}
}
