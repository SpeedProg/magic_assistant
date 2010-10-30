package com.reflexit.magiccards.ui.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class ImageCreator {
	static private ImageCreator instance;

	private ImageCreator() {
	}

	static synchronized public ImageCreator getInstance() {
		if (instance == null)
			instance = new ImageCreator();
		return instance;
	}

	LinkedHashMap<String, IMagicCard> editionImageQueue = new LinkedHashMap<String, IMagicCard>();
	Job editionImageLoadingJob = new Job("Loading set images") {
		{
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			while (true) {
				IMagicCard card = null;
				String key;
				synchronized (editionImageQueue) {
					if (editionImageQueue.size() > 0) {
						key = editionImageQueue.keySet().iterator().next();
						card = editionImageQueue.get(key);
						editionImageQueue.remove(key);
					} else
						return Status.OK_STATUS;
				}
				try {
					URL url = CardCache.createSetImageURL(card, true);
					Image image = MagicUIActivator.getDefault().getImageRegistry().get(key);
					if (image == null && url != null) {
						image = ImageCreator.createNewSetImage(url);
						MagicUIActivator.getDefault().getImage(key, image);
					}
				} catch (Exception e) {
					// no image, skip
				}
			}
		}
	};

	public static Image createNewSetImage(URL url) {
		try {
			ImageDescriptor imageDesc = ImageDescriptor.createFromURL(url);
			Image origImage = imageDesc.createImage();
			final int width = origImage.getBounds().width;
			final int height = origImage.getBounds().height;
			float zoom = 1;
			int size = 12;
			int x, y;
			if (width > height) {
				zoom = size / (float) width;
				x = 0;
				y = (int) ((size - height * zoom) / 2);
			} else {
				zoom = size / (float) height;
				y = 0;
				x = (int) ((size - width * zoom) / 2);
			}
			Image scaledImage = new Image(Display.getDefault(), origImage.getImageData().scaledTo((int) (width * zoom),
					(int) (height * zoom)));
			Image centeredImage = new Image(Display.getDefault(), size, size);
			GC newGC = new GC(centeredImage);
			newGC.drawImage(scaledImage, x, y);
			newGC.dispose();
			return centeredImage;
		} catch (SWTException e) {
			System.err.println("Cannot load image: " + url + ": " + e.getMessage());
			return null;
		}
	}

	public Image getSetImage(IMagicCard card) {
		URL url = null;
		try {
			url = CardCache.createSetImageURL(card, false);
			if (url == null)
				return null;
			String key = url.toExternalForm();
			Image image = MagicUIActivator.getDefault().getImageRegistry().get(key);
			File file = new File(url.getFile());
			if (image == null) {
				if (file.exists()) {
					image = ImageCreator.createNewSetImage(url);
					if (image == null)
						return null;
					return MagicUIActivator.getDefault().getImage(key, image);
				} else {
					synchronized (editionImageQueue) {
						editionImageQueue.put(key, card);
					}
					editionImageLoadingJob.schedule(0);
					return null;
				}
			} else {
				return image;
			}
		} catch (SWTException e) {
			// failed to create image
			MagicUIActivator.log("Failed to create an image: " + url);
			MagicUIActivator.log(e);
		} catch (IOException e) {
			// huh
		}
		return null;
	}
}
