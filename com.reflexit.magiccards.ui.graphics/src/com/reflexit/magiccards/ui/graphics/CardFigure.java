package com.reflexit.magiccards.ui.graphics;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.model.IMagicCard;

public class CardFigure extends XFigure {
	private static final int FULL_OPAQUE = 255;
	protected Point mousePos;
	private IMagicCard card;

	public CardFigure(XFigure parent, ImageData imageData, IMagicCard card) {
		super(parent);
		this.bounds = new Rectangle(0, 0, imageData.width, imageData.height);
		setAlphaBlendingForCorners(imageData);
		Image transparentIdeaImage = new Image(Display.getCurrent(), imageData);
		this.image = transparentIdeaImage;
		this.card = card;
	}

	private void setAlphaBlendingForCorners(ImageData fullImageData) {
		int width = fullImageData.width;
		int height = fullImageData.height;
		int redMask = fullImageData.palette.redMask;
		int blueMask = fullImageData.palette.blueMask;
		int greenMask = fullImageData.palette.greenMask;
		byte[] alphaData = new byte[height * width];
		int[] lineData = new int[width];
		for (int y = 0; y < height; y++) {
			fullImageData.getPixels(0, y, width, lineData, 0);
			byte[] alphaRow = new byte[width];
			for (int x = 0; x < width; x++) {
				int radius = 8;
				int al = FULL_OPAQUE;
				int x1 = width / 2 - Math.abs(x - width / 2) - radius;
				int y1 = height / 2 - Math.abs(y - height / 2) - radius;
				if (y1 < 0 && x1 < 0) {
					int pixelValue = lineData[x];
					int r = (pixelValue & redMask) >>> -fullImageData.palette.redShift;
					int g = (pixelValue & greenMask) >>> -fullImageData.palette.greenShift;
					int b = (pixelValue & blueMask) >>> -fullImageData.palette.blueShift;
					int al1 = al - (r + g + b) / 3;
					if (al1 < 10) {
						double dist = Math.sqrt(x1 * x1 + y1 * y1);
						if (dist > radius - 1)
							al = al1;
					} else
						al = al1;
				}
				alphaRow[x] = (byte) al;
			}
			System.arraycopy(alphaRow, 0, alphaData, y * width, width);
		}
		fullImageData.alphaData = alphaData;
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
