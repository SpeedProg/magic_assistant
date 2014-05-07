package com.reflexit.magiccards.ui.views.collector;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.ui.views.LazyTreeViewerManager;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public class CollectorManager extends LazyTreeViewerManager implements IDisposable {
	protected CollectorManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		hookDragAndDrop();
		// getViewer().setComparator(new CollectorViewerComparator());
		return control;
	}

	@Override
	protected ColumnCollection doGetColumnCollection(String prefPageId) {
		// return super.doGetColumnCollection(prefPageId);
		return new CollectorColumnCollection();
	}
}
