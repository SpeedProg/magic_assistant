package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.utils.SymbolConverter;

public class CostColumn extends AbstractColumn implements Listener {
	public CostColumn() {
		super(MagicCardField.COST);
	}

	@Override
	public String getColumnName() {
		return "Cost";
	}

	public Image getActualImage(Object element) {
		if (element instanceof IMagicCard) {
			return SymbolConverter.buildCostImage(((IMagicCard) element).getCost());
		}
		return null;
	}

	@Override
	protected void handleEraseEvent(Event event) {
		event.detail &= ~SWT.FOREGROUND;
	}

	@Override
	public ICardField getSortField() {
		return MagicCardField.CMC;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IMagicCard) { // cost
			return String.valueOf(((IMagicCard) element).get(MagicCardField.CMC));
		}
		return null;
	}

	@Override
	public int getColumnWidth() {
		return 100;
	}

	@Override
	public void handlePaintEvent(Event event) {
		if (event.index == this.columnIndex) { // cost
			Item item = (Item) event.item;
			Object row = item.getData();
			if (!(row instanceof IMagicCard))
				return;
			int x = event.x;
			int y = event.y;
			GC gc = event.gc;
			String text = getText(row);
			int tx = x + event.width;
			if (text != null) {
				tx = x + getBounds(event).width - gc.textExtent(text).x - 2;
				gc.drawText(text, tx, y + 1, true);
			}
			Image costImage = getActualImage(row);
			if (costImage != null) {
				gc.setClipping(x, y, tx-x, event.height);
				gc.drawImage(costImage, x, y + 1);
			}
		}
	}
}
