package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.utils.SymbolConverter;

public class CostColumn extends AbstractColumn implements Listener {
	public CostColumn() {
		super(MagicCardField.COST);
	}

	@Override
	public String getText(Object element) {
		return "     ";
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

	public void handleEvent(Event event) {
		if (event.index == this.columnIndex) { // cost
			Item item = (Item) event.item;
			Object row = item.getData();
			int x = event.x;
			int y = event.y;
			String cost;
			if (row instanceof IMagicCard) {
				cost = ((IMagicCard) row).getCost();
			} else
				return;
			String text = getActualText(row);
			Rectangle bounds;
			if (item instanceof TableItem)
				bounds = ((TableItem) item).getBounds(event.index);
			else if (item instanceof TreeItem)
				bounds = ((TreeItem) item).getBounds(event.index);
			else
				return;
			int tx = 0;
			int ty = 0;
			if (text != null) {
				Point tw = event.gc.textExtent(text);
				tx = bounds.width - tw.x - 1;
				ty = tw.y;
				if (tx < 0)
					tx = 0;
				event.gc.setClipping(x, y, tx, bounds.height);
			}
			int imageHeight = 12;
			int yi = y + (Math.max(bounds.height - imageHeight, 2)) / 2;
			Image costImage = SymbolConverter.buildCostImage(cost);
			event.gc.drawImage(costImage, x + 2, yi);
			if (text != null) {
				int yt = y + bounds.height - 2 - ty;
				event.gc.setClipping(x, yt, bounds.width, bounds.height);
				event.gc.drawText(text, x + tx - 5, yt, true);
			}
		}
	}

	@Override
	public ICardField getSortField() {
		return MagicCardField.CMC;
	}

	private String getActualText(Object element) {
		if (element instanceof IMagicCard) { // cost
			return ((IMagicCard) element).get(MagicCardField.CMC) + "";
		}
		return null;
	}

	@Override
	public int getColumnWidth() {
		return 100;
	}
}
