package com.reflexit.magiccards.ui.graphics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class XFigure {
	protected Image image;
	protected Rectangle bounds;
	protected XFigure parent;

	public XFigure(XFigure parent) {
		super();
		this.parent = parent;
	}

	public Image getImage() {
		return image;
	}

	public void redraw() {
		GC gc = new GC(image);
		paint(gc);
		gc.dispose();
	}

	public void paint(GC gc) {
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
		bounds.x = x;
		bounds.y = y;
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public void dispose() {
		image.dispose();
	}
}