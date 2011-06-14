package com.reflexit.magiccards.ui.graphics;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class DeskFigure extends XFigure {
	ArrayList<CardFigure> children;
	CardFigure active;
	private DeskCanvas canvas;
	private IFilteredCardStore<IMagicCard> fstore;
	private final Rectangle DEFAULT_SIZE = new Rectangle(0, 0, 1200, 768);

	public DeskFigure(DeskCanvas deskCanvas) {
		super(null);
		this.canvas = deskCanvas;
		children = new ArrayList<CardFigure>();
		image = new Image(Display.getCurrent(), DEFAULT_SIZE.width, DEFAULT_SIZE.height);
		bounds = image.getBounds();
		redraw();
	}

	@Override
	public void redraw() {
		super.redraw();
		canvas.redraw();
	}

	public void moveUp(CardFigure card) {
		children.remove(card);
		children.add(card);
	}

	@Override
	public void paint(GC gc) {
		// background
		gc.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
		gc.fillRectangle(0, 0, bounds.width, bounds.height);
		gc.drawLine(0, 0, bounds.width, bounds.height);
		gc.drawLine(0, bounds.height, bounds.width, 0);
		gc.drawText("Default Image", 10, 10);
		// children
		for (CardFigure child : children) {
			child.paint(gc);
		}
	}

	@Override
	public boolean mouseDrag(Point p) {
		if (active != null) {
			Rectangle cb = active.getBounds();
			if (cb.contains(p)) {
				active.mouseDrag(p);
				return true;
			}
			active.mouseStopDrag(p);
			active = null;
		}
		return false;
	}

	@Override
	public boolean mouseStartDrag(Point p) {
		active = null;
		for (int i = children.size() - 1; i >= 0; i--) {
			CardFigure child = children.get(i);
			if (child.getBounds().contains(p)) {
				active = child;
				break;
			}
		}
		if (active == null)
			return false;
		active.mouseStartDrag(p);
		moveUp(active);
		return true;
	}

	@Override
	public boolean mouseStopDrag(Point p) {
		if (active == null)
			return false;
		active.mouseStopDrag(p);
		return true;
	}

	public void setInput(IFilteredCardStore<IMagicCard> store) {
		if (this.fstore == store)
			return;
		this.fstore = store;
		for (Iterator<CardFigure> iterator = children.iterator(); iterator.hasNext();) {
			CardFigure next = iterator.next();
			if (store.contains(next.getCard())) {
				continue;
			}
			next.dispose();
			iterator.remove();
		}
		int i = 0;
		for (IMagicCard card : store) {
			String path = CardCache.createLocalImageFilePath(card);
			if (path != null && new File(path).exists()) {
				ImageData cimage = new ImageData(path);
				CardFigure c1 = new CardFigure(this, cimage, card);
				c1.setLocation(100 + 20 * i, 100 + 20 * i++);
				children.add(c1);
				System.err.println(cimage.width + " " + cimage.height);
			} else {
				Image notfound = createCardNotFoundImage(card);
				CardFigure c1 = new CardFigure(this, notfound.getImageData(), card);
				children.add(c1);
				notfound.dispose();
			}
		}
		redraw();
	}

	private Image createCardNotFoundImage(IMagicCard card) {
		Image im = createCardNotFoundImage();
		GC gc = new GC(im);
		gc.setAntialias(SWT.ON);
		gc.setFont(canvas.getFont());
		gc.drawText(card.getName(), 20, 16, true);
		gc.drawText("Image not found", 30, 46, true);
		gc.dispose();
		return im;
	}

	private Image createCardNotFoundImage() {
		int width = 223;
		int height = 310;
		Image im = MagicUIActivator.getDefault().getImage("icons/template.png");
		return getResized(im, width, height);
	}

	private Image getResized(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height);
		gc.dispose();
		return scaled;
	}

	public void resize() {
		Rectangle client = canvas.getClientArea();
		Rectangle newsize = DEFAULT_SIZE;
		if (newsize.width < client.width)
			newsize.width = client.width;
		if (newsize.height < client.height)
			newsize.height = client.height;
		image = new Image(Display.getCurrent(), newsize.width, newsize.height);
		bounds = image.getBounds();
		super.redraw();
	}
}
