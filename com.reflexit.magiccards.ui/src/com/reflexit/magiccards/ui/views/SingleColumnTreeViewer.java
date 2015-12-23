package com.reflexit.magiccards.ui.views;

import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.model.RootTreeViewerContentProvider;

public class SingleColumnTreeViewer extends ExtendedTreeViewer {
	public SingleColumnTreeViewer(Composite parent) {
		super(parent);
		setColumnCollection(doGetColumnCollection(""));
		setContentProvider(new RootTreeViewerContentProvider());
		setAutoExpandLevel(2);
		getTree().setHeaderVisible(false);
		GroupColumn labelProvider = new GroupColumn(true, true, false);
		setLabelProvider(labelProvider);
	}

	@Override
	protected void createFillerColumn() {
		// not
	}

	@Override
	protected void createLabelProviders() {
		// no
	}
}
