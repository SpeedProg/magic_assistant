/**
 * 
 */
package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.ui.utils.SymbolConverter;

public class MagicCardLabelProvider extends LabelProvider implements ITableLabelProvider, Listener {
	/**
	 * @param memoryTreeViewerManager
	 */
	public MagicCardLabelProvider() {
	}

	public String getText(Object obj) {
		return null;
	}

	public Image getImage(Object obj) {
		return null;
	}

	public Image getActualColumnImage(Object element, int columnIndex) {
		if (element instanceof IMagicCard && columnIndex == 2) { // cost
			return SymbolConverter.buildImage(((IMagicCard) element).getByIndex(columnIndex));
		}
		if (element instanceof MagicCardPhisical && columnIndex == 2 + 6) {
			return SymbolConverter.buildImage(((MagicCardPhisical) element).getCard().getByIndex(columnIndex - 6));
		}
		return null;
	}

	public String getActualColumnText(Object element, int columnIndex) {
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			if (columnIndex == 0) { // id
				return String.valueOf(card.getCardId());
			}
			if (columnIndex == 4 || columnIndex == 5) {
				Object x = card.getObjectByIndex(columnIndex);
				return getPower(x);
			}
			return card.getByIndex(columnIndex);
		} else if (element instanceof MagicCardPhisical) {
			if (columnIndex < 6) {
				return ((MagicCardPhisical) element).getByIndex(columnIndex);
			} else {
				return getActualColumnText(((MagicCardPhisical) element).getCard(), columnIndex - 6);
			}
		}
		return null;
	}

	private String getPower(Object x) {
		return x.toString();
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IMagicCard && columnIndex == 2) { // cost
			return null;
		}
		if (element instanceof MagicCardPhisical && columnIndex == 2 + 6) {
			return null;
		}
		return getActualColumnText(element, columnIndex);
	}

	public void handleEvent(Event event) {
		if (event.index == 2 && event.item.getData() instanceof IMagicCard) { // cost
			TableItem item = (TableItem) event.item;
			IMagicCard row = (IMagicCard) item.getData();
			int x = event.x, y = event.y;
			Image image = getActualColumnImage(row, event.index);
			if (image != null)
				event.gc.drawImage(image, x, y);
			String text = getActualColumnText(row, event.index);
			if (text != null)
				event.gc.drawText(text, x + image.getBounds().width + 5, y);
		}
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}