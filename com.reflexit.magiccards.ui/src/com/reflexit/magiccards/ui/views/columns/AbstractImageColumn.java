package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import com.reflexit.magiccards.core.model.abs.ICardField;

public abstract class AbstractImageColumn extends GenColumn implements Listener {
	protected boolean cannotPaintImage = false;

	public AbstractImageColumn(ICardField field, String name) {
		super(field, name);
	}

	@Override
	public String getToolTipText(Object element) {
		String text = getText(element);
		if (text == null)
			return null;
		if (text.isEmpty())
			return null;
		return text;
	}

	protected Image getActualImage(Object row) {
		return null;
	};

	@Override
	public int getColumnWidth() {
		return 40;
	}

	public void paintCellWithImage(Event event, int imageWidth) {
		if (!isVisible())
			return;// no paint of invisible column
		Item item = (Item) event.item;
		Object row = item.getData();
		// int x = event.x;
		int y = event.y;
		Rectangle bounds = getBounds(event);
		int x = bounds.x;
		// int y = bounds.y;
		int w = bounds.width;
		int h = bounds.height;
		int leftMargin = 0;
		Image image = getActualImage(row);
		if (image != null) {
			imageWidth = Math.max(imageWidth, image.getBounds().width);
			leftMargin = imageWidth;
			Rectangle imageBounds = image.getBounds();
			event.gc.drawImage(image, x + (imageWidth - imageBounds.width) / 2, y + (h - imageBounds.height) / 2);
		}
		paintCellText(event, row, y, x, w, h, leftMargin);
	}

	protected void paintCellText(Event event, Object row, int y, int x, int w, int h, int leftMargin) {
		String text = getText(row);
		if (text != null) {
			x += leftMargin;
			event.gc.setClipping(x, y, w - 3 - leftMargin, h);
			event.gc.drawText(text, x + 3, y + 1, true);
		}
	}

	@Override
	public void handleEvent(Event event) {
		// if (event.type == SWT.PaintItem) {
		// cannotPaintImage = false;
		// }
		if (event.index == this.columnIndex || this.columnIndex == -1) {
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
		if (cannotPaintImage)
			return;
		event.detail &= ~SWT.FOREGROUND;
	}

	public void handlePaintEvent(Event event) {
		paintCellWithImage(event, -1);
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

	@Override
	public Image getImage(Object element) {
		if (cannotPaintImage)
			return getActualImage(element);
		return super.getImage(element);
	}
}
