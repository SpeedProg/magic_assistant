package com.reflexit.magiccards.ui.gallery;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.ui.utils.ImageCache;

final class MagicCardImageLabelProvider extends LabelProvider {
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
		return "";
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
		Image im = cache.getImage(card, () -> refreshCallback(card, element));
		if (im != null)
			return im;
		return cache.CARD_NOT_FOUND_IMAGE_TEMPLATE;
	}

	protected void refreshCallback(final IMagicCard card, final Object element) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (cache.getCachedImage(card) != null) {
					viewer.refresh(element, true);
					// System.err.println("setting real image for " + element);
				}
				// item.setImage(image);
				// item.getParent().redraw();
			}
		});
	}
}