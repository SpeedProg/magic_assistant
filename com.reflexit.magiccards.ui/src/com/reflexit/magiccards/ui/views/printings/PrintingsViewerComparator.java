package com.reflexit.magiccards.ui.views.printings;

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardComparator;

public class PrintingsViewerComparator extends ViewerComparator {
	private Comparator comparator;

	public PrintingsViewerComparator() {
		super();
	}

	@Override
	protected Comparator getComparator() {
		return comparator;
	}

	public void setOrder(ICardField field, boolean asc) {
		comparator = MagicCardComparator.getComparator(field, asc);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return getComparator().compare(e1, e2);
	}
}
