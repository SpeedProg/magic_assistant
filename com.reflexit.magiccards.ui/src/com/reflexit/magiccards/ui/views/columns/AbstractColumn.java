package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;

public abstract class AbstractColumn extends ColumnLabelProvider {
	protected final ICardField dataIndex;
	protected int columnIndex = -1;
	protected int userwidth;
	private boolean visible = true;

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public AbstractColumn(ICardField dataIndex) {
		this.dataIndex = dataIndex;
		this.userwidth = getColumnWidth();
	}

	public abstract String getColumnName();

	@Override
	public String getText(Object element) {
		if (element instanceof ICard) {
			ICard card = (ICard) element;
			try {
				ICardField field = getDataField();
				Object value = card.get(field);
				if (value == null)
					return "";
				return value.toString();
			} catch (ArrayIndexOutOfBoundsException e) {
				return "n/a";
			}
		}
		return "?";
	}

	public String getColumnTooltip() {
		if (getColumnName().equals(getColumnFullName()))
			return "";
		else
			return getColumnFullName();
	}

	public int getColumnWidth() {
		return 100;
	}

	public ICardField getDataField() {
		return this.dataIndex;
	}

	public ICardField getSortField() {
		return getDataField();
	}

	public String getColumnFullName() {
		return getColumnName();
	}

	/**
	 * @param viewer
	 * @return
	 */
	public EditingSupport getEditingSupport(ColumnViewer viewer) {
		return null;
	}

	public int getUserWidth() {
		return userwidth;
	}

	public void setUserWidth(int width) {
		this.userwidth = width;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void updateOnEdit(final ColumnViewer viewer, IMagicCard card) {
		Object input = viewer.getInput();
		if (input instanceof IFilteredCardStore) {
			// update
			((IFilteredCardStore) input).getCardStore().reindex();
			DataManager.getInstance().update(card, null);
		} else {
			// update
			viewer.refresh(true);
		}
	}

	@Override
	public Color getBackground(Object element) {
		boolean own = false;
		if (element instanceof IMagicCardPhysical) {
			own = ((IMagicCardPhysical) element).isOwn();
		}
		return MagicUIActivator.getDefault().getBgColor(own);
	}

	@Override
	public String toString() {
		return getColumnName();
	}

	public void handleEvent(Event event) {
		if (event.index == this.columnIndex) {
			if (event.type == SWT.EraseItem) {
				handleEraseEvent(event);
			} else if (event.type == SWT.MeasureItem) {
				handleMeasureEvent(event);
			} else if (event.type == SWT.PaintItem) {
				handlePaintEvent(event);
			}
		}
	}

	protected void handleMeasureEvent(Event event) {
		// do nothing
	}

	protected void handleEraseEvent(Event event) {
		// do nothing
	}

	protected void handlePaintEvent(Event event) {
		// do nothing
	}

	protected static Rectangle getBounds(Event event) {
		Item item = (Item) event.item;
		Rectangle bounds = null;
		if (item instanceof TableItem)
			bounds = ((TableItem) item).getBounds(event.index);
		else if (item instanceof TreeItem)
			bounds = ((TreeItem) item).getBounds(event.index);
		return bounds;
	}

	public void paintCellWithImage(Event event, int imageWidth) {
		Item item = (Item) event.item;
		Object row = item.getData();
		int x = event.x;
		int y = event.y;
		Rectangle bounds = getBounds(event);
		int w = bounds.width;
		int h = bounds.height;
		int leftMargin = 0;
		Image image = getActualImage(row);
		if (image != null) {
			imageWidth = Math.max(imageWidth, image.getBounds().width);
			leftMargin = imageWidth;
			Rectangle imageBounds = image.getBounds();
			event.gc.drawImage(image, x + (imageWidth - imageBounds.width) / 2, y + (h - imageBounds.height)
					/ 2);
		}
		String text = getText(row);
		if (text != null) {
			event.gc.setClipping(x, y, w - 3, h);
			event.gc.drawText(text, x + 3 + leftMargin, y + 1, true);
		}
	}

	protected abstract Image getActualImage(Object row);
}
