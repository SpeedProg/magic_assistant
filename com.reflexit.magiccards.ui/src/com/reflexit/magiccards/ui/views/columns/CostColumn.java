package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.ui.utils.SymbolConverter;

public class CostColumn extends AbstractImageColumn {
	public CostColumn() {
		super(MagicCardField.COST, "Cost");
	}

	@Override
	public Image getActualImage(Object element) {
		if (element instanceof IMagicCard) {
			return SymbolConverter.buildCostImage(((IMagicCard) element).getCost());
		}
		return null;
	}

	@Override
	public ICardField getSortField() {
		return MagicCardField.CMC;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			return getFullCost((IMagicCard) element, cannotPaintImage);
		}
		return null;
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof IMagicCard) {
			return getFullCost((IMagicCard) element, true);
		}
		return null;
	}

	protected String getFullCost(IMagicCard element, boolean withColors) {
		String cmc = String.valueOf(element.get(MagicCardField.CMC));
		String cost = element.getCost();
		if (cost == null)
			cost = "";
		if (cost.equals("*"))
			return cost;
		if (withColors) {
			return cost + " = " + cmc;
		}
		return cmc;
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
			Rectangle bounds = getBounds(event);
			// int x = event.x;
			int x = bounds.x; // bug in gtk?
			int y = event.y;
			GC gc = event.gc;
			String text = getText(row);
			int tx = x + event.width;
			if (text != null) {
				tx = x + bounds.width - gc.textExtent(text).x - 5;
				gc.drawText(text, tx, y + 1, true);
			}
			Image costImage = getActualImage(row);
			if (costImage != null) {
				gc.setClipping(x, y, tx - x, event.height);
				gc.drawImage(costImage, x, y + 1);
			}
		}
	}
}
