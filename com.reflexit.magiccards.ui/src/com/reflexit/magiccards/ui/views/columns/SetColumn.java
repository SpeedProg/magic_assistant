package com.reflexit.magiccards.ui.views.columns;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.CardList;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.ImageCreator;
import com.reflexit.magiccards.ui.widgets.ComboStringEditingSupport;

public class SetColumn extends AbstractImageColumn implements Listener {
	public final static int SET_IMAGE_WIDTH = ImageCreator.SET_IMG_WIDTH;
	private boolean showImage = false;

	public boolean isShowImage() {
		return showImage;
	}

	public void setShowImage(boolean showImage) {
		this.showImage = showImage;
	}

	public SetColumn() {
		super(MagicCardField.SET, "Set");
	}

	public SetColumn(boolean showImage) {
		this();
		this.showImage = showImage;
	}

	@Override
	public Color getBackground(Object element) {
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			if (card.getCardId() == 0 && element instanceof MagicCardPhysical)
				return Display.getDefault().getSystemColor(SWT.COLOR_RED);
		}
		return super.getBackground(element);
	}

	@Override
	public int getColumnWidth() {
		return 150;
	}

	@Override
	public Image getActualImage(Object element) {
		if (isShowImage()) {
			IMagicCard card = getCardElement(element);
			return ImageCreator.getInstance().getSetImage(card);
		}
		return null;
	}

	@Override
	public Image getImage(Object element) {
		if (cannotPaintImage) {
			return getClippedImage(element);
		}
		return null;
	}

	public Image getClippedImage(Object element) {
		// normally image is rendered, however on linux in certain gtk level
		// this is not working so we will do default image but we have to
		// clip it
		Image image = getActualImage(element);
		if (image == null)
			return null;
		String key = getImageKey(element);
		if (key == null)
			return null;
		key = key.replace("file:", "clipped:");
		Image image2 = MagicUIActivator.getDefault().getImage(key);
		if (image2 == null) {
			ImageData nImage = ImageCreator.getInstance().scaleAndCenter(image.getImageData(),
					ImageCreator.SET_IMG_WIDTH, ImageCreator.SET_IMG_HEIGHT, false);
			image2 = new Image(Display.getCurrent(), nImage);
			MagicUIActivator.getDefault().getImageRegistry().put(key, image2);
		}
		return image2;
	}

	public String getImageKey(Object element) {
		IMagicCard card = getCardElement(element);
		if (card != null) {
			try {
				URL url = CardCache.createSetImageURL(card, false);
				return url.toExternalForm();
			} catch (IOException e) {
				// ignore
			}
		}
		return null;
	}

	public IMagicCard getCardElement(Object element) {
		IMagicCard card = null;
		if (element instanceof ICardGroup) {
			CardGroup cardGroup = (CardGroup) element;
			String set = cardGroup.getSet();
			if (set != null && set.length() > 0 && !set.equals("*")) {
				card = cardGroup.getFirstCard();
			}
		} else if (element instanceof IMagicCard) {
			card = (IMagicCard) element;
		}
		return card;
	}

	@Override
	public void handlePaintEvent(Event event) {
		if (event.index == this.columnIndex) { // our column
			paintCellWithImage(event, SET_IMAGE_WIDTH);
		}
	}

	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		return new SetEditingSupport(viewer);
	}

	public class SetEditingSupport extends ComboStringEditingSupport {
		public SetEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected boolean canEdit(Object element) {
			if (element instanceof MagicCardPhysical)
				return true;
			else
				return false;
		}

		@Override
		public int getStyle() {
			return SWT.NONE;
		}

		@Override
		public String[] getItems(Object element) {
			IMagicCardPhysical card = (IMagicCardPhysical) element;
			Collection<IMagicCard> cards = DataManager.getInstance().getMagicDBStore().getCandidates(card.getName());
			CardList list = new CardList(cards);
			Set<Object> unique = list.getUnique(MagicCardField.SET);
			unique.add(card.getSet());
			String sets[] = unique.toArray(new String[unique.size()]);
			return sets;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof MagicCardPhysical) {
				IMagicCardPhysical card = (IMagicCardPhysical) element;
				return card.getSet();
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof MagicCardPhysical) {
				MagicCardPhysical card = (MagicCardPhysical) element;
				// set
				Collection<IMagicCard> cards = DataManager.getInstance().getMagicDBStore()
						.getCandidates(card.getName());
				String set = (String) value;
				String oldSet = card.getSet();
				if (oldSet != null && oldSet.equals(set))
					return;
				for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
					IMagicCard base = (IMagicCard) iterator.next();
					if (base.getSet().equals(set)) {
						card.setMagicCard((MagicCard) base);
						updateOnEdit(getViewer(), card);
						return;
					}
				}
				MagicUIActivator.log("Cannot set new set for " + card + " of value " + set);
			}
		}
	}
}
