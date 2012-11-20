package com.reflexit.magiccards.ui.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.CachedImageNotFoundException;
import com.reflexit.magiccards.core.CannotDetermineSetAbbriviation;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.ui.MagicUIActivator;

/**
 * Create or loads images for cards
 * 
 */
public class ImageCreator {
	private static final String TEXT_ITALIC_FONT_KEY = "text_italic";
	private static final String TEXT_FONT_KEY = "text";
	private static final String TYPE_FONT_KEY = "type";
	private static final String TITLE_FONT_KEY = "title";
	private static final String CARD_TEMPLATE = "card_template";
	static private ImageCreator instance;
	private FontRegistry fontRegistry;

	private ImageCreator() {
		// private
		fontRegistry = new FontRegistry(Display.getCurrent());
		String fontName = "Times New Roman";
		fontRegistry.put(TITLE_FONT_KEY, new FontData[] { new FontData(fontName, 9, SWT.BOLD) });
		fontRegistry.put(TYPE_FONT_KEY, new FontData[] { new FontData(fontName, 8, SWT.BOLD) });
		fontRegistry.put(TEXT_FONT_KEY, new FontData[] { new FontData(fontName, 7, SWT.NORMAL) });
		fontRegistry.put(TEXT_ITALIC_FONT_KEY, new FontData[] { new FontData(fontName, 7, SWT.ITALIC) });
	}

	public Font getFount(String key) {
		return fontRegistry.get(key);
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
			MagicUIActivator.log("Cannot load image: " + url + ": " + e.getMessage());
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
	 * Get card image from local cache. This image is not managed - to be disposed by called.
	 * 
	 * @param card
	 * @param remote
	 *            - attempt to load from web
	 * @param forceUpdate
	 *            - force update from web
	 * @return returns image or throws FileNotFoundException if image is mot found locally or cannot
	 *         be downloaded remotely
	 * @throws IOException
	 */
	public Image getCardImage(IMagicCard card, boolean remote, boolean forceUpdate) throws IOException, CannotDetermineSetAbbriviation,
			SWTException {
		synchronized (card) {
			if (forceUpdate)
				remote = true;
			String path = CardCache.createLocalImageFilePath(card);
			try {
				File file = new File(path);
				if (file.exists() && remote == false) {
					return createCardImage(path);
				}
				if (remote == false)
					throw new CachedImageNotFoundException(path);
				file = CardCache.downloadAndSaveImage(card, remote, forceUpdate);
				return createCardImage(file.getAbsolutePath());
			} catch (SWTException e) {
				// failed to create image
				MagicUIActivator.log("Failed to create an image for: " + card);
				MagicUIActivator.log(e);
				throw e;
			}
		}
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

	public Image getRotated(Image image, int angle) {
		int dir = 0;
		switch (angle) {
			case 180:
				dir = SWT.DOWN;
				break;
			case 90:
				dir = SWT.RIGHT;
				break;
			case -90:
				dir = SWT.LEFT;
				break;
			default:
				break;
		}
		ImageData data = rotate(image.getImageData(), dir);
		return new Image(image.getDevice(), data);
	}

	public ImageData rotate(ImageData srcData, int direction) {
		int bytesPerPixel = srcData.bytesPerLine / srcData.width;
		int destBytesPerLine = (direction == SWT.DOWN) ? srcData.bytesPerLine : srcData.height * bytesPerPixel;
		byte[] newData = new byte[(direction == SWT.DOWN) ? srcData.data.length : srcData.width * destBytesPerLine];
		int width = 0, height = 0;
		for (int srcY = 0; srcY < srcData.height; srcY++) {
			for (int srcX = 0; srcX < srcData.width; srcX++) {
				int destX = 0, destY = 0, destIndex = 0, srcIndex = 0;
				switch (direction) {
					case SWT.LEFT: // left 90 degrees
						destX = srcY;
						destY = srcData.width - srcX - 1;
						width = srcData.height;
						height = srcData.width;
						break;
					case SWT.RIGHT: // right 90 degrees
						destX = srcData.height - srcY - 1;
						destY = srcX;
						width = srcData.height;
						height = srcData.width;
						break;
					case SWT.DOWN: // 180 degrees
						destX = srcData.width - srcX - 1;
						destY = srcData.height - srcY - 1;
						width = srcData.width;
						height = srcData.height;
						break;
				}
				destIndex = (destY * destBytesPerLine) + (destX * bytesPerPixel);
				srcIndex = (srcY * srcData.bytesPerLine) + (srcX * bytesPerPixel);
				System.arraycopy(srcData.data, srcIndex, newData, destIndex, bytesPerPixel);
			}
		}
		// destBytesPerLine is used as scanlinePad to ensure that no padding is required
		return new ImageData(width, height, srcData.depth, srcData.palette, srcData.scanlinePad, newData);
	}

	public Image createCardNotFoundImage() {
		int width = 223;
		int height = 310;
		Image im = MagicUIActivator.getDefault().getImage("icons/template.png");
		Image im2 = getResized(im, width, height);
		ImageData data = im2.getImageData();
		setAlphaBlendingForCorners(data);
		Image transparentImage = new Image(Display.getCurrent(), data);
		im2.dispose();
		return transparentImage;
	}

	public Image getCardNotFoundImageTemplate() {
		String key = CARD_TEMPLATE;
		Image image = MagicUIActivator.getDefault().getImageRegistry().get(key);
		if (image != null)
			return image;
		return MagicUIActivator.getDefault().getImage(key, createCardNotFoundImage());
	}

	public Image createCardNotFoundImage(IMagicCard card) {
		Image im1 = getCardNotFoundImageTemplate();
		Image im = new Image(Display.getCurrent(), im1, SWT.IMAGE_COPY);
		GC gc = new GC(im);
		// gc.setAntialias(SWT.ON);
		// gc.setInterpolation(SWT.HIGH);
		gc.setFont(getFount(TITLE_FONT_KEY));
		gc.drawText(card.getName(), 20, 17, true);
		Image costImage = SymbolConverter.buildCostImage(card.getCost());
		gc.drawImage(costImage, 204 - costImage.getBounds().width, 18);
		gc.setFont(getFount(TYPE_FONT_KEY));
		gc.drawText(card.getType() == null ? "Uknown Type" : card.getType(), 20, 175, true);
		gc.setFont(getFount(TEXT_FONT_KEY));
		gc.drawText("Image not found", 30, 46, true);
		// String oracleText = card.getOracleText();
		// oracleText = oracleText.replaceAll("<br>", "\n");
		// gc.drawText(oracleText, 20, 200, true);
		gc.dispose();
		return im;
	}

	private static final int FULL_OPAQUE = 255;

	public void setAlphaBlendingForCorners(ImageData fullImageData) {
		int width = fullImageData.width;
		int height = fullImageData.height;
		// int redMask = fullImageData.palette.redMask;
		// int blueMask = fullImageData.palette.blueMask;
		// int greenMask = fullImageData.palette.greenMask;
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
				// int pixelValue = lineData[x];
				// int r = (pixelValue & redMask) >>> -fullImageData.palette.redShift;
				// int g = (pixelValue & greenMask) >>> -fullImageData.palette.greenShift;
				// int b = (pixelValue & blueMask) >>> -fullImageData.palette.blueShift;
				// int al2 = al - (r + g + b) / 3;
				if (y1 < 0 && x1 < 0) {
					double dist = Math.sqrt(x1 * x1 + y1 * y1);
					if (dist >= radius)
						al = 0;
					else if (dist >= radius - 1)
						al = (int) (FULL_OPAQUE * (radius - dist));
				}
				alphaRow[x] = (byte) al;
			}
			System.arraycopy(alphaRow, 0, alphaData, y * width, width);
		}
		fullImageData.alphaData = alphaData;
	}
}
