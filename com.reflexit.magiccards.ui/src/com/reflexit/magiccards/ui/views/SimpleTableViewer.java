package com.reflexit.magiccards.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.model.SortOrderViewerComparator;

public class SimpleTableViewer extends ExtendedTableViewer {
	public SimpleTableViewer(Composite parent, ColumnCollection collection) {
		super(parent, collection);
	}

	@Override
	public void setSortColumn(int index, int direction) {
		super.setSortColumn(index, direction);
		if (index >= 0) {
			AbstractColumn man = (AbstractColumn) getColumnViewer().getLabelProvider(index);
			SortOrderViewerComparator vcomp = manager.getViewerComparator();
			vcomp.setOrder(man.getSortField(), getSortDirection() == SWT.UP);
			getColumnViewer().setComparator(vcomp);
		} else {
			getColumnViewer().setComparator(null);
		}
	}
	// @Override
	// protected void doUpdateItem(Widget widget, Object element, boolean
	// fullMap) {
	// System.err.println("request to update item: " + element);
	// super.doUpdateItem(widget, element, fullMap);
	// }
}
