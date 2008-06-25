package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.ui.utils.SymbolConverter;

public class CostColumn extends ColumnManager implements Listener {
	public CostColumn(int column) {
		super(column);
	}

	public String getText(Object element) {
		return "     ";
	}

	public String getColumnName() {
		return "Cost";
	}

	public Image getActualImage(Object element) {
		if (element instanceof MagicCardPhisical) {
			element = ((MagicCardPhisical) element).getCard();
		}
		if (element instanceof IMagicCard) {
			return SymbolConverter.buildImage(((IMagicCard) element).getByIndex(this.dataIndex));
		}
		return null;
	}

	public void handleEvent(Event event) {
		if (event.index == this.dataIndex) { // cost
			TableItem item = (TableItem) event.item;
			Object row = item.getData();
			int x = event.x, y = event.y;
			Image image = getActualImage(row);
			int iw = 0;
			if (image != null) {
				event.gc.drawImage(image, x, y);
				iw = image.getBounds().width;
			}
			String text = getActualText(row);
			if (text != null)
				event.gc.drawText(text, x + iw + 5, y);
		}
	}

	private String getActualText(Object element) {
		if (element instanceof MagicCardPhisical) {
			element = ((MagicCardPhisical) element).getCard();
		}
		if (element instanceof IMagicCard) { // cost
			return ((IMagicCard) element).getCmc() + "";
		}
		return null;
	}

	public int getColumnWidth() {
		return 100;
	}
}
