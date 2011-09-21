package com.reflexit.magiccards.ui.graphics;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class XFigure extends EventManager {
	protected Image image;
	protected XFigure parent;
	protected Point location;
	private boolean selected = false;

	public XFigure(XFigure parent) {
		super();
		this.parent = parent;
		this.location = new Point(0, 0);
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public void redraw(int x, int y, int width, int height) {
		GC gc = new GC(image);
		paint(gc, x, y, width, height);
		gc.dispose();
	}

	public void redraw() {
		Rectangle bounds = image.getBounds();
		redraw(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	public void paint(GC gc) {
		return;
	}

	public void paint(GC gc, int x, int y, int width, int height) {
		return;
	}

	public boolean mouseDrag(Point p) {
		return false;
	}

	public boolean mouseStartDrag(Point p) {
		return false;
	}

	public boolean mouseStopDrag(Point p) {
		return false;
	}

	public void setLocation(int x, int y) {
		location.x = x;
		location.y = y;
	}

	public Rectangle getBounds() {
		Rectangle rect = image.getBounds();
		rect.x = location.x;
		rect.y = location.y;
		return rect;
	}

	public void dispose() {
		image.dispose();
		image = null;
	}

	public void setSelected(boolean b) {
		this.selected = b;
	}

	public boolean isSelected() {
		return selected;
	}
}