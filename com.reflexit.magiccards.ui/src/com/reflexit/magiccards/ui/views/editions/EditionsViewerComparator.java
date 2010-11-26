package com.reflexit.magiccards.ui.views.editions;

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class EditionsViewerComparator extends ViewerComparator {
	private EditionsComparator comparator;

	public EditionsViewerComparator() {
		super();
		comparator = EditionsComparator.getComparator(EditionField.NAME, false);
	}

	@Override
	protected Comparator getComparator() {
		return comparator;
	}

	public void setOrder(EditionField field, boolean asc) {
		comparator = EditionsComparator.getComparator(field, asc);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return getComparator().compare(e1, e2);
	}
}
