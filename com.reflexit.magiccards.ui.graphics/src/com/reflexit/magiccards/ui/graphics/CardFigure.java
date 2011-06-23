package com.reflexit.magiccards.ui.graphics;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.ui.utils.ImageCreator;

public class CardFigure extends XFigure {
	protected Point mousePos;
	private IMagicCard card;
	private boolean imageNotFound;

	public boolean isImageNotFound() {
		return imageNotFound;
	}

	public CardFigure(XFigure parent, ImageData imageData, IMagicCard card) {
		super(parent);
		this.card = card;
		this.bounds = new Rectangle(0, 0, imageData.width, imageData.height);
		ImageCreator.getInstance().setAlphaBlendingForCorners(imageData);
		Image transparentIdeaImage = new Image(Display.getCurrent(), imageData);
		this.image = transparentIdeaImage;
		this.imageNotFound = false;
	}

	public CardFigure(XFigure parent, IMagicCard card) {
		super(parent);
		this.card = card;
		image = ImageCreator.getInstance().createCardNotFoundImage(card);
		Rectangle bi = image.getBounds();
		this.bounds = new Rectangle(0, 0, bi.width, bi.height);
		this.imageNotFound = true;
	}

	@Override
	public void paint(GC gc) {
		gc.drawImage(image, bounds.x, bounds.y);
	}

	@Override
	public boolean mouseStartDrag(Point p) {
		mousePos = new Point(p.x, p.y);
		return true;
	}

	@Override
	public boolean mouseDrag(Point p) {
		if (mousePos != null) {
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
			bounds.x = x;
			bounds.y = y;
			parent.redraw();
			mousePos = p;
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
}
