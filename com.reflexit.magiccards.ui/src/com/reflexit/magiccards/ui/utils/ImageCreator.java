package com.reflexit.magiccards.ui.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.CannotDetermineSetAbbriviation;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class ImageCreator {
	static private ImageCreator instance;

	private ImageCreator() {
		// private
	}

	static synchronized public ImageCreator getInstance() {
		if (instance == null)
			instance = new ImageCreator();
		return instance;
	}

	private LinkedHashMap<String, IMagicCard> editionImageQueue = new LinkedHashMap<String, IMagicCard>();
	private Job editionImageLoadingJob = new Job("Loading set images") {
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
			if (image == null) {
				File file = new File(url.getFile());
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

	/**
	 * Get card image from local cache. This image is not managed - to be
	 * disposed by called.
	 * 
	 * @param card
	 * @param remote
	 *            - attempt to load from web
	 * @param forceUpdate
	 *            - force update from web
	 * @return returns image or throws FileNotFoundException if image is mot
	 *         found locally or cannot be downloaded remotely
	 * @throws IOException
	 */
	public Image getCardImage(IMagicCard card, boolean remote, boolean forceUpdate) throws IOException, CannotDetermineSetAbbriviation,
			SWTException {
		String path = CardCache.createLocalImageFilePath(card);
		try {
			File file = new File(path);
			if (file.exists()) {
				return createCardImage(path);
			}
			if (remote == false)
				throw new FileNotFoundException(path);
			file = CardCache.downloadAndSaveImage(card, remote, forceUpdate);
			return createCardImage(path);
		} catch (SWTException e) {
			// failed to create image
			MagicUIActivator.log("Failed to create an image for: " + card);
			MagicUIActivator.log(e);
			throw e;
		}
	}

	/**
	 * Check that card image exists locally or schedule a loading job if image
	 * not found. This image is not managed - to be disposed by called.
	 * 
	 * @param card
	 * @throws IOException
	 */
	public void loadCardImageOffline(IMagicCard card, boolean forceUpdate) throws IOException, CannotDetermineSetAbbriviation, SWTException {
		CardCache.loadCardImageOffline(card, forceUpdate);
	}

	private Image createCardImage(String path) {
		Image image = new Image(Display.getCurrent(), path);
		return image;
	}

	public Image getResized(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height);
		gc.dispose();
		return scaled;
	}

	public Image createCardNotFoundImage() {
		int width = 223;
		int height = 310;
		Image im = MagicUIActivator.getDefault().getImage("icons/template.png");
		return getResized(im, width, height);
	}

	public Image createCardNotFoundImage(IMagicCard card) {
		Image im = createCardNotFoundImage();
		GC gc = new GC(im);
		gc.setAntialias(SWT.ON);
		// gc.setFont(canvas.getFont());
		gc.drawText(card.getName(), 20, 16, true);
		gc.drawText("Image not found", 30, 46, true);
		gc.dispose();
		return im;
	}
}
