package com.reflexit.magiccards.ui.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

public class ScrollableCanvas extends Canvas implements MouseWheelListener, MouseListener, MouseMoveListener {
	private ScrollableCanvas canvas;
	private Image image;
	protected Point origin;
	private boolean dragCanvas = true;
	private Point mousePos;
	private ScrollBar hBar;
	private ScrollBar vBar;

	public ScrollableCanvas(Composite parent, Image originalImage) {
		super(parent, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.DOUBLE_BUFFERED);
		canvas = this;
		image = originalImage;
		origin = new Point(0, 0);
		hBar = canvas.getHorizontalBar();
		vBar = canvas.getVerticalBar();
		hBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				scroll(hBar.getSelection(), vBar.getSelection());
			}
		});
		vBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				scroll(hBar.getSelection(), vBar.getSelection());
			}
		});
		canvas.addMouseWheelListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMoveListener(this);
		canvas.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				resize();
			}
		});
		canvas.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e) {
				GC gc = e.gc;
				paint(gc);
			}
		});
	}

	public void scroll(int hSelection, int vSelection) {
		// System.err.println("scroll: " + +hSelection + "," + vSelection);
		// System.err.println("mp=" + mousePos + " ... sb=" +
		// hBar.getSelection() + "," + vBar.getSelection() + " or=" + origin);
		int destY = -vSelection - origin.y;
		int destX = -hSelection - origin.x;
		Rectangle rect = image.getBounds();
		canvas.scroll(destX, destY, 0, 0, rect.width, rect.height, false);
		origin.y = -vSelection;
		origin.x = -hSelection;
	}

	public boolean isDragCanvas() {
		return dragCanvas;
	}

	public void setDragCanvas(boolean dragCanvas) {
		this.dragCanvas = dragCanvas;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
		resize();
	}

	public void mouseScrolled(MouseEvent e) {
		if (e.count == 0)
			return;
		int vSelection = vBar.getSelection();
		vBar.setSelection(vSelection - e.count * vBar.getPageIncrement());
		// scroll(0, e.count * 2);
	}

	public void mouseUp(MouseEvent e) {
		if (mousePos != null) {
			mouseStopDrag(e);
		}
		mousePos = null;
	}

	public void mouseStopDrag(MouseEvent e) {
	}

	public void mouseDown(MouseEvent e) {
		if (e.button == 1) {
			mousePos = new Point(e.x, e.y);
			mouseStartDrag(e);
		}
	}

	public void mouseStartDrag(MouseEvent e) {
	}

	public void mouseDoubleClick(MouseEvent e) {
		// ignore
	}

	public void mouseMove(MouseEvent e) {
		if (mousePos != null) {
			mouseDrag(e);
			Point newpoint = new Point(e.x, e.y);
			if (dragCanvas) {
				hBar.setSelection(hBar.getSelection() + mousePos.x - newpoint.x);
				vBar.setSelection(vBar.getSelection() + mousePos.y - newpoint.y);
				scroll(hBar.getSelection(), vBar.getSelection());
			}
			mousePos = newpoint;
		}
	}

	public void mouseDrag(MouseEvent e) {
	}

	public void resize() {
		Rectangle rect = image.getBounds();
		Rectangle client = canvas.getClientArea();
		hBar.setMaximum(rect.width);
		vBar.setMaximum(rect.height);
		hBar.setThumb(Math.min(rect.width, client.width));
		vBar.setThumb(Math.min(rect.height, client.height));
		int hPage = rect.width - client.width;
		int vPage = rect.height - client.height;
		int hSelection = hBar.getSelection();
		int vSelection = vBar.getSelection();
		if (hSelection >= hPage) {
			if (hPage <= 0)
				hSelection = 0;
			origin.x = -hSelection;
		}
		if (vSelection >= vPage) {
			if (vPage <= 0)
				vSelection = 0;
			origin.y = -vSelection;
		}
		canvas.redraw();
	}

	public void paint(GC gc) {
		Rectangle client = canvas.getClientArea();
		gc.fillRectangle(0, 0, client.width, client.height);
		gc.drawImage(image, origin.x, origin.y);
		// Rectangle rect = image.getBounds();
		//
		// int marginWidth = client.width - rect.width;
		// if (marginWidth > 0) {
		// gc.fillRectangle(rect.width, 0, marginWidth, client.height);
		// }
		// int marginHeight = client.height - rect.height;
		// if (marginHeight > 0) {
		// gc.fillRectangle(0, rect.height, client.width, marginHeight);
		// }
	}
}
