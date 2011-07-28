package com.reflexit.magiccards.ui.graphics;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class DeskCanvas extends ScrollableCanvas {
	DeskFigure desk;

	public DeskCanvas(Composite parent) {
		super(parent, null);
		desk = new DeskFigure(this);
	}

	@Override
	public void mouseDrag(MouseEvent e) {
		Point p = new Point(e.x - origin.x, e.y - origin.y);
		if (desk.mouseDrag(p)) {
			setDragCanvas(false);
		}
	}

	@Override
	public void mouseStartDrag(MouseEvent e) {
		Point p = new Point(e.x - origin.x, e.y - origin.y);
		if (desk.mouseStartDrag(p)) {
			setDragCanvas(false);
		}
	}

	@Override
	public void mouseStopDrag(MouseEvent e) {
		Point p = new Point(e.x - origin.x, e.y - origin.y);
		desk.mouseStopDrag(p);
		setDragCanvas(true);
	}

	public void setInput(IFilteredCardStore store) {
		desk.setInput(store);
	}

	@Override
	public void resize() {
		desk.resize();
		super.resize();
	}
}
