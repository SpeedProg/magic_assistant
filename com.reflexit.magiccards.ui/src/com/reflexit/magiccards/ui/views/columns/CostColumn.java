package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.ui.utils.SymbolConverter;

public class CostColumn extends ColumnManager implements Listener {
	public CostColumn(int column) {
		super(column);
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
			return SymbolConverter.buildImage(((IMagicCard) element).getByIndex(this.dataIndex));
		}
		return null;
	}

	public void handleEvent(Event event) {
		if (event.index == this.dataIndex) { // cost
			Item item = (Item) event.item;
			Object row = item.getData();
			int x = event.x;
			int y = event.y;
			String cost;
			if (row instanceof IMagicCard) {
				cost = ((IMagicCard) row).getByIndex(this.dataIndex);
			} else
				return;
			String text = getActualText(row);
			Point tw = null;
			Rectangle bounds;
			if (item instanceof TableItem)
				bounds = ((TableItem) item).getBounds(event.index);
			else if (item instanceof TreeItem)
				bounds = ((TreeItem) item).getBounds(event.index);
			else
				return;
			int tx = 0;
			if (text != null) {
				tw = event.gc.textExtent(text);
				tx = bounds.width - tw.x - 1;
				if (tx < 0)
					tx = 0;
				event.gc.setClipping(x, y, tx, bounds.height);
			}
			int imageHeight = 12;
			int yi = y + (Math.max(bounds.height - imageHeight, 2)) / 2;
			SymbolConverter.drawManaImage(event.gc, cost, x + 2, yi);
			if (text != null) {
				int yt = y + bounds.height - 2 - tw.y;
				event.gc.setClipping(x, yt, bounds.width, bounds.height);
				event.gc.drawText(text, x + tx, yt, true);
			}
		}
	}

	@Override
	public int getSortIndex() {
		return IMagicCard.INDEX_CMC;
	}

	private String getActualText(Object element) {
		if (element instanceof IMagicCard) { // cost
			return ((IMagicCard) element).getCmc() + "";
		}
		return null;
	}

	@Override
	public int getColumnWidth() {
		return 100;
	}
}
