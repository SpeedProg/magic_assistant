package com.reflexit.magiccards.ui.graphics;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class DeskCanvas extends ScrollableCanvas {
	private DesktopFigure desktop;

	public DeskCanvas(Composite parent) {
		super(parent, null);
		desktop = new DesktopFigure(this);
	}

	@Override
	public void mouseDrag(MouseEvent e) {
		Point p = new Point(e.x - origin.x, e.y - origin.y);
		if (desktop.mouseDrag(p)) {
			setDragCanvas(false);
		}
	}

	@Override
	public void mouseStartDrag(MouseEvent e) {
		Point p = new Point(e.x - origin.x, e.y - origin.y);
		if (desktop.mouseStartDrag(p)) {
			setDragCanvas(false);
		}
	}

	@Override
	public void mouseStopDrag(MouseEvent e) {
		Point p = new Point(e.x - origin.x, e.y - origin.y);
		desktop.mouseStopDrag(p);
		setDragCanvas(true);
	}

	public void setInput(IFilteredCardStore store) {
		desktop.setInput(store);
	}

	@Override
	public void resize() {
		if (desktop != null)
			desktop.resize();
		else
			super.resize();
	}
}
