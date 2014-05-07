package com.reflexit.magiccards.ui.graphics;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.services.IDisposable;

import com.reflexit.magiccards.ui.utils.StoredSelectionProvider;

public class XFigure extends StoredSelectionProvider implements IDisposable {
	private Image image;
	protected XFigure parent;
	protected Point location;
	private boolean selected = false;

	public XFigure(XFigure parent, int width, int height) {
		super();
		this.parent = parent;
		this.location = new Point(0, 0);
		this.image = new Image(Display.getCurrent(), width, height);
	}

	public Image getImage() {
		return image;
	}

	private void setImage(Image image) {
		if (this.image != null)
			this.image.dispose();
		this.image = image;
	}

	public void redraw(int x, int y, int width, int height) {
		if (image.isDisposed())
			throw new IllegalArgumentException("image is disposed");
		try {
			GC gc = new GC(image);
			try {
				paint(gc, x, y, width, height);
			} finally {
				gc.dispose();
			}
		} catch (IllegalArgumentException e) {
			Activator.log(e);
		}
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
		if (image != null)
			image.dispose();
		image = null;
	}

	public void setSelected(boolean b) {
		this.selected = b;
	}

	public boolean isSelected() {
		return selected;
	}

	@Override
	public String toString() {
		return getBounds().toString();
	}

	/**
	 * Resize will reset the default image, after that all references to image must be re-set
	 * 
	 * @param newsize
	 */
	public void resize(Rectangle newsize) {
		setImage(new Image(Display.getCurrent(), newsize.width, newsize.height));
		// System.err.println("resize " + this);
	}
}