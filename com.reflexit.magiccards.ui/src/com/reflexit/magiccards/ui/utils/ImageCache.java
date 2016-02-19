package com.reflexit.magiccards.ui.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Image;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.utils.MRUCache;
import com.reflexit.magiccards.ui.MagicUIActivator;

/**
 * This class manages Card images, it is singleton and its resposible for
 * disposing them. Do not dispose these images!
 * 
 * @author elaskavaia
 *
 */
public class ImageCache {
	public final Image CARD_NOT_FOUND_IMAGE_TEMPLATE = ImageCreator.getInstance().getCardNotFoundImageTemplate();
	public static ImageCache INSTANCE = new ImageCache();
	private HashMap<Object, Image> map = new MRUCache<Object, Image>(500) {
		@Override
		protected boolean removeEldestEntry(Map.Entry eldest) {
			if (super.removeEldestEntry(eldest)) {
				Image image = (Image) eldest.getValue();
				if (image != CARD_NOT_FOUND_IMAGE_TEMPLATE) {
					image.dispose();
					eldest.setValue(null);
				}
				return true;
			}
			return false;
		};
	};
	private int cart;

	private ImageCache() {
	};

	public Image getCachedImage(Object element) {
		return map.get(element);
	}

	/**
	 * Images from this map will be disposed except
	 * CARD_NOT_FOUND_IMAGE_TEMPLATE
	 * 
	 * @param key
	 * @param value
	 */
	public void setCachedImage(Object key, Image value) {
		map.put(key, value);
	}

	/**
	 * If image is in cache it immediately returns, otherwise callback will be
	 * called when image is ready or error happened
	 * 
	 * @param element
	 * @param callback
	 * @return
	 */
	public Image getImage(Object element, final Runnable callback) {
		// System.err.println("getting image for " + element + " " +
		// element.getClass());
		Image image = map.get(element);
		if (image != null)
			return image;
		if (element instanceof ICardGroup) {
			return null;
		}
		if (!(element instanceof IMagicCard)) {
			return null;
		}
		final IMagicCard card = (IMagicCard) element;
		if (in() == false)
			return null;
		new Job("Loading card image " + card) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					try {
						String path = ImageCreator.getInstance().createCardPath(card, true, false);
						final Image image = ImageCreator.getInstance().createCardImage(path, false);
						if (image != null) {
							map.put(card, image);
							callback.run();
						}
					} catch (IOException e) {
						MagicUIActivator.log(e);
					}
					return Status.OK_STATUS;
				} finally {
					out();
				}
			}
		}.schedule();
		return null;
	}

	protected synchronized boolean in() {
		if (cart > 50)
			return false;
		cart++;
		return true;
	}

	protected synchronized void out() {
		cart--;
	}
}
