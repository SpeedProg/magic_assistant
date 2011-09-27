package com.reflexit.magiccards.ui.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.ui.utils.ImageCreator;

public class CardFigure extends XFigure {
	private Image cardImage;
	private Point mousePos;
	private IMagicCard card;
	private boolean imageNotFound;

	public boolean isImageNotFound() {
		return imageNotFound;
	}

	public CardFigure(XFigure parent, ImageData imageData, IMagicCard card) {
		super(parent, imageData.width, imageData.height);
		this.card = card;
		setImageData(imageData);
	}

	public void setCardImage(Image im) {
		if (cardImage != null)
			cardImage.dispose();
		cardImage = im;
	}

	public CardFigure(XFigure parent, IMagicCard card) {
		super(parent, 223, 310);
		this.card = card;
		Image im = ImageCreator.getInstance().createCardNotFoundImage(card);
		setCardImage(im);
		this.imageNotFound = true;
	}

	public void setImageData(ImageData imageData) {
		ImageCreator.getInstance().setAlphaBlendingForCorners(imageData);
		Image im = new Image(Display.getCurrent(), imageData);
		setCardImage(im);
		imageNotFound = false;
	}

	@Override
	public void paint(GC gc) {
		gc.drawImage(cardImage, location.x, location.y);
	}

	@Override
	public void paint(GC gc, int x, int y, int width, int height) {
		// System.err.println("Clipping " + x + "," + y + "," + width + "," +
		// height);
		Rectangle clip = new Rectangle(x, y, width, height);
		Rectangle cb = getBounds();
		Rectangle in = clip.intersection(cb);
		if (in.isEmpty())
			return;
		gc.drawImage(cardImage, in.x - cb.x, in.y - cb.y, in.width, in.height, in.x, in.y, in.width, in.height);
		if (isSelected()) {
			gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			gc.drawFocus(cb.x, cb.y, cb.width, cb.height);
		}
	}

	@Override
	public boolean mouseStartDrag(Point p) {
		mousePos = new Point(p.x, p.y);
		return true;
	}

	@Override
	public boolean mouseDrag(Point p) {
		if (mousePos != null) {
			Rectangle bounds = getBounds();
			int x = bounds.x - mousePos.x + p.x;
			if (x < 0)
				x = 0;
			int y = bounds.y - mousePos.y + p.y;
			if (y < 0)
				y = 0;
			Rectangle pb = parent.getBounds();
			if (x > pb.width - bounds.width)
				x = pb.width - bounds.width;
			if (y > pb.height - bounds.height)
				y = pb.height - bounds.height;
			mousePos = p;
			setLocation(x, y);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseStopDrag(Point p) {
		if (mousePos == null)
			return false;
		mousePos = null;
		return true;
	}

	public IMagicCard getCard() {
		return card;
	}

	@Override
	public String toString() {
		return card + " at " + super.toString();
	}
}
