package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.reflexit.magiccards.core.model.SortOrder;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class SortOrderViewerComparator extends ViewerComparator {
	public SortOrderViewerComparator() {
		super(new SortOrder());
	}

	public void setOrder(ICardField field, boolean asc) {
		((SortOrder) getComparator()).setSortField(field, asc);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return getComparator().compare(e1, e2);
	}
}
