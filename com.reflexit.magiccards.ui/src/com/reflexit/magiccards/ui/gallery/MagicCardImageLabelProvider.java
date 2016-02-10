package com.reflexit.magiccards.ui.gallery;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.ui.utils.ImageCache;
import com.reflexit.magiccards.ui.utils.WaitUtils;

final class MagicCardImageLabelProvider extends LabelProvider implements IImageOverlayRenderer {
	private StructuredViewer viewer;

	public MagicCardImageLabelProvider(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	private ImageCache cache = ImageCache.INSTANCE;

	@Override
	public void dispose() {
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ICardGroup) {
			ICardGroup group = (ICardGroup) element;
			return group.getName();
		} else if (element instanceof IMagicCard) {
			return ((IMagicCard) element).getName();
		}
		return String.valueOf(element);
	}

	@Override
	public Image getImage(final Object element) {
		// System.err.println("getting image for " + element + " " +
		// element.getClass());
		Object candidate = element;
		if (element instanceof CardGroup) {
			CardGroup cardGroup = (CardGroup) element;
			candidate = cardGroup.getFirstCard();
		}
		if (!(candidate instanceof IMagicCard)) {
			return null;
		}
		final IMagicCard card = (IMagicCard) candidate;
		Image im = cache.getImage(card, () -> refreshCallback(element));
		if (im != null)
			return im;
		return cache.CARD_NOT_FOUND_IMAGE_TEMPLATE;
	}

	protected void refreshCallback(final Object element) {
		if (viewer == null)
			return;
		WaitUtils.asyncExec(() -> viewer.refresh(element, true));
	}

	@Override
	public void drawAllOverlays(GC gc, Object element, int x, int y, Point imageSize, int xShift, int yShift) {
		String text = getCountDecoration(element);
		if (!text.isEmpty()) {
			gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
			int x1 = x + xShift - 5;
			int y1 = y + yShift + imageSize.y - 20 + 5;
			gc.fillOval(x1, y1, 20, 20);
			gc.drawText(text, x1, y1, true);
		}
	}

	private String getCountDecoration(Object element) {
		if (element instanceof MagicCard) {
			return "";
		}
		if (element instanceof CardGroup && ((CardGroup) element).getFirstCard() instanceof MagicCard) {
			return "";
		}
		if (element instanceof IMagicCardPhysical) {
			String text = "x" + ((IMagicCardPhysical) element).getCount();
			return text;
		}
		return "";
	}
}