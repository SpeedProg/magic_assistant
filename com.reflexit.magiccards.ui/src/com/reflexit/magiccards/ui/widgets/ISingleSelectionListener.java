package com.reflexit.magiccards.ui.widgets;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public interface ISingleSelectionListener extends SelectionListener {
	@Override
	public default void widgetDefaultSelected(SelectionEvent e) {
	};
}
