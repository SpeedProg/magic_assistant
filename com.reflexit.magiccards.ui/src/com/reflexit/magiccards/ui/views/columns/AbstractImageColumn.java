package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.reflexit.magiccards.core.model.abs.ICardField;

public abstract class AbstractImageColumn extends GenColumn implements Listener {
	public AbstractImageColumn(ICardField field, String name) {
		super(field, name);
	}

	@Override
	protected void handleEraseEvent(Event event) {
		event.detail &= ~SWT.FOREGROUND;
	}

	@Override
	public String getToolTipText(Object element) {
		String text = getText(element);
		if (text == null) return null;
		if (text.isEmpty()) return null;
		return text;
	}

	@Override
	public void handlePaintEvent(Event event) {
		if (event.index == this.columnIndex) { // our column
			paintCellWithImage(event, -1);
		}
	}

	@Override
	protected String getActualText(Object row) {
		return null;
	}

	@Override
	public int getColumnWidth() {
		return 40;
	}
}
