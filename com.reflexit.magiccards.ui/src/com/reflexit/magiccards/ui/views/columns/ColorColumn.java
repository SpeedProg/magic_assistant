package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;

import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.utils.SymbolConverter;

public class ColorColumn extends GenColumn implements Listener {
	public ColorColumn() {
		super(MagicCardField.COLOR, "Color");
	}

	@Override
	protected void handleEraseEvent(Event event) {
		event.detail &= ~SWT.FOREGROUND;
	}

	@Override
	public String getText(Object element) {
		String text = super.getText(element);
		if (text.length() == 0)
			return text;
		return Colors.getColorName(text.toString());
	}

	@Override
	public String getToolTipText(Object element) {
		return getText(element);
	}

	@Override
	public Image getActualImage(Object element) {
		String text = super.getText(element);
		if (text.length() == 0)
			return null;
		String icost = Colors.getInstance().getColorIdentityAsCost(text);
		return SymbolConverter.buildCostImage(icost);
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
			int tx = x + event.width;
			Image costImage = getActualImage(row);
			if (costImage != null) {
				gc.setClipping(x, y, tx - x, event.height);
				gc.drawImage(costImage, x, y + 1);
			}
		}
	}

	@Override
	public int getColumnWidth() {
		return 40;
	}
}
