package com.reflexit.magiccards.ui.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
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
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.CachedImageNotFoundException;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.core.sync.WebUtils;
import com.reflexit.magiccards.ui.MagicUIActivator;

/**
 * Create or loads images for cards
 *
 */
public class ImageCreator {
	public static final int SET_IMG_HEIGHT = 16;
	public static final int SET_IMG_WIDTH = 32;
	private static final String TEXT_ITALIC_FONT_KEY = "text_italic";
	private static final String TEXT_FONT_KEY = "text";
	private static final String TYPE_FONT_KEY = "type";
	private static final String TITLE_FONT_KEY = "title";
	private static final String CARD_TEMPLATE = "card_template";
	static private ImageCreator instance;
	private FontRegistry fontRegistry;

	private ImageCreator() {
		// private
	}

	public synchronized void initFontRegistry() {
		if (fontRegistry == null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = fontRegistry.defaultFont().getFontData()[0].getName();
			fontRegistry.put(TITLE_FONT_KEY, new FontData[] { new FontData(fontName, 9, SWT.BOLD) });
			fontRegistry.put(TYPE_FONT_KEY, new FontData[] { new FontData(fontName, 8, SWT.BOLD) });
			fontRegistry.put(TEXT_FONT_KEY, new FontData[] { new FontData(fontName, 7, SWT.NORMAL) });
			fontRegistry.put(TEXT_ITALIC_FONT_KEY, new FontData[] { new FontData(fontName, 7, SWT.ITALIC) });
		}
	}

	public Font getFont(String key) {
		initFontRegistry();
		return fontRegistry.get(key);
	}

	static synchronized public ImageCreator getInstance() {
		if (instance == null) {
			instance = new ImageCreator();
		}
		return instance;
	}

	private LinkedHashMap<String, IMagicCard> editionImageQueue = new LinkedHashMap<String, IMagicCard>();
	private Job editionImageLoadingJob = new Job("Loading set images") {
		{
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			while (MagicUIActivator.getDefault() != null) {
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
						if (image != null)
							MagicUIActivator.getDefault().getImageRegistry().put(key, image);
					}
				} catch (Exception e) {
					// no image, skip
				}
			}
			return Status.CANCEL_STATUS;
		}
	};

	public static Image createSetNotFoundImage(@SuppressWarnings("unused") String rarity) {
		Display display = Display.getDefault();
		Image im = new Image(display, 12, 12);
		GC gc = new GC(im);
		gc.drawText("?", 0, 0);
		gc.dispose();
		ImageData im2 = scaleAndCenter(im.getImageData(), SET_IMG_WIDTH, SET_IMG_HEIGHT, false);
		im.dispose();
		return new Image(display, im2);
	}

	public static Image createNewSetImage(URL url) {
		try {
			ImageDescriptor imageDesc = ImageDescriptor.createFromURL(url);
			Display display = Display.getDefault();
			if (imageDesc.getImageData() == null) {
				MagicUIActivator.log("Cannot load image: " + url + ": null imageData");
				return null;
			}
			// scaleAndCenter(imageDesc.getImageData(), SET_IMG_WIDTH,
			// SET_IMG_HEIGHT, false);
			ImageData scaleAndCenter = imageDesc.getImageData();
			return new Image(display, scaleAndCenter);
		} catch (SWTException e) {
			MagicUIActivator.log("Cannot load image: " + url + ": " + e.getMessage());
			return null;
		}
	}

	public static ImageData scaleAndCenter(ImageData imageData, int nwidth, int nheight, boolean scale) {
		final int width = imageData.width;
		final int height = imageData.height;
		float zoom;
		if (width * nheight > nwidth * height) {
			zoom = nwidth / (float) width;
		} else {
			zoom = nheight / (float) height;
		}
		if (scale == false)
			zoom = 1; // do not scale
		int zwdth = (int) (width * zoom);
		int zheight = (int) (height * zoom);
		int x = (nwidth - zwdth) / 2;
		int y = (nheight - zheight) / 2;
		int ox = 0;
		int oy = 0;
		if (x < 0) {
			ox = -x;
			x = 0;
		}
		if (y < 0) {
			oy = -y;
			y = 0;
		}
		Display display = Display.getDefault();
		ImageData scaledData = imageData.scaledTo(zwdth, zheight);
		Image scaledImage = new Image(display, scaledData);
		Image centeredImage = new Image(display, nwidth, nheight);
		GC newGC = new GC(centeredImage);
		int dwidth = zoom != 1 ? nwidth : scaledData.width - 2 * ox;
		int dheight = zoom != 1 ? nheight : scaledData.height - 2 * oy;
		newGC.drawImage(scaledImage, ox, oy, dwidth, dheight, x, y, dwidth, dheight);
		newGC.dispose();
		scaledImage.dispose();
		ImageData finalImageData = centeredImage.getImageData();
		if (finalImageData.transparentPixel == -1) {
			try {
				finalImageData.transparentPixel = finalImageData.palette.getPixel(new RGB(255, 255, 255));
			} catch (IllegalArgumentException e) {
				// pallete does not have white hmm
				e.printStackTrace();
			}
		}
		centeredImage.dispose();
		return finalImageData;
	}

	public Image getSetImage(IMagicCard card) {
		if (card == null)
			return null;
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
					if (image == null) {
						image = ImageCreator.createSetNotFoundImage(card.getRarity());
					}
					return MagicUIActivator.getDefault().getImage(key, image);
				} else {
					if (card.getGathererId() != 0) {
						synchronized (editionImageQueue) {
							editionImageQueue.put(key, card);
						}
						editionImageLoadingJob.schedule(0);
					}
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
	public String createCardPath(IMagicCard card, boolean remote, boolean forceUpdate) throws IOException {
		synchronized (card) {
			if (forceUpdate)
				remote = true;
			if (WebUtils.isWorkOffline())
				remote = false;
			String path = CardCache.createLocalImageFilePath(card);
			try {
				File file = new File(path);
				if (file.exists() && remote == false) {
					return path;
				}
				if (remote == false)
					throw new CachedImageNotFoundException(path);
				file = CardCache.downloadAndSaveImage(card, remote, forceUpdate);
				return file.getAbsolutePath();
			} catch (IOException e) {
				// failed to create image
				MagicUIActivator.log("Failed to create an image for: " + card);
				throw e;
			}
		}
	}

	public Image createCardImage(String path, boolean resize) {
		try {
			ImageData data = resize ? getResizedCardImage(new ImageData(path)) : new ImageData(path);
			setAlphaBlendingForCorners(data);
			return new Image(Display.getDefault(), data);
		} catch (SWTException e) {
			// failed to create image
			MagicUIActivator.log("Failed to create an image for: " + path);
			MagicUIActivator.log(e);
			return null;
		}
	}

	public ImageData getResizedCardImage(ImageData data) {
		int width = data.width;
		int height = data.height;
		float ratio = width / (float) height;
		if (ratio > 0.68 && ratio < 0.73) {
			// regular card
			// gather cards are 223 x 310, if card is bigger lets resize it
			if (height > 320) {
				return data.scaledTo((int) (310 * ratio), 310);
			}
		}
		return data;
	}

	public Image getResized(Image origImage, int width, int height) {
		return new Image(Display.getDefault(), origImage.getImageData().scaledTo(width, height));
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
		int width = 0, height = 0;
		switch (direction) {
		case SWT.LEFT: // left 90 degrees
			width = srcData.height;
			height = srcData.width;
			break;
		case SWT.RIGHT: // right 90 degrees
			width = srcData.height;
			height = srcData.width;
			break;
		case SWT.DOWN: // 180 degrees
			width = srcData.width;
			height = srcData.height;
			break;
		}
		int scanlinePad = srcData.scanlinePad;
		int bytesPerLine = (((width * srcData.depth + 7) / 8) + (scanlinePad - 1)) / scanlinePad * scanlinePad;
		int minBytesPerLine = srcData.type == SWT.IMAGE_PNG ? ((((width + 7) / 8) + 3) / 4) * 4 : bytesPerLine;
		int destBytesPerLine = (direction == SWT.DOWN) ? srcData.bytesPerLine : minBytesPerLine;
		byte[] newData = new byte[(direction == SWT.DOWN) ? srcData.data.length : height * destBytesPerLine];
		for (int srcY = 0; srcY < srcData.height; srcY++) {
			for (int srcX = 0; srcX < srcData.width; srcX++) {
				int destX = 0, destY = 0, destIndex = 0, srcIndex = 0;
				switch (direction) {
				case SWT.LEFT: // left 90 degrees
					destX = srcY;
					destY = srcData.width - srcX - 1;
					break;
				case SWT.RIGHT: // right 90 degrees
					destX = srcData.height - srcY - 1;
					destY = srcX;
					break;
				case SWT.DOWN: // 180 degrees
					destX = srcData.width - srcX - 1;
					destY = srcData.height - srcY - 1;
					break;
				}
				destIndex = (destY * destBytesPerLine) + (destX * bytesPerPixel);
				srcIndex = (srcY * srcData.bytesPerLine) + (srcX * bytesPerPixel);
				System.arraycopy(srcData.data, srcIndex, newData, destIndex, bytesPerPixel);
			}
		}
		// destBytesPerLine is used as scanlinePad to ensure that no padding is
		// required
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
		final Display d = Display.getDefault();
		Image[] imres = new Image[1];
		d.syncExec(() -> {
			Image im1 = getCardNotFoundImageTemplate();
			Image im = new Image(Display.getCurrent(), im1, SWT.IMAGE_COPY);
			GC gc = new GC(im);
			// gc.setAntialias(SWT.ON);
			// gc.setInterpolation(SWT.HIGH);
			gc.setFont(getFont(TITLE_FONT_KEY));
			gc.drawText(card.getName(), 20, 17, true);
			Image costImage = SymbolConverter.buildCostImage(card.getCost());
			if (costImage != null)
				gc.drawImage(costImage, 204 - costImage.getBounds().width, 18);
			gc.setFont(getFont(TYPE_FONT_KEY));
			gc.drawText(card.getType() == null ? "Uknown Type" : card.getType(), 20, 175, true);
			gc.setFont(getFont(TEXT_FONT_KEY));
			gc.drawText("Image not found", 30, 46, true);
			String oracleText = card.getOracleText();
			renderHtml(gc, 18, 195, 180, 80, oracleText == null ? "" : oracleText);
			// oracleText = oracleText.replaceAll("<br>", "\n");
			// gc.drawText(oracleText, 20, 200, true);
			gc.setFont(getFont(TITLE_FONT_KEY));
			String pt = "";
			String tou = card.getToughness();
			if (tou != null && tou.length() > 0) {
				pt = card.getPower() + "/" + tou;
			}
			gc.drawText(pt, 204 - 20, 283, true);
			Image set = getSetImage(card);
			if (set != null)
				gc.drawImage(set, 204 - set.getBounds().width, 177);
			gc.dispose();
			imres[0] = im;
		});
		return imres[0];
	}

	private void renderHtml(GC parentGc, int x, int y, int w, int h, String html) {
		String text = html;
		text = text.replaceAll("<br>", "\n");
		text = text.replaceAll("<i>", "");
		text = text.replaceAll("</i>", "");
		final TextLayout layout = new TextLayout(parentGc.getDevice());
		layout.setText(text);
		layout.setWidth(w - 10);
		layout.setFont(parentGc.getFont());
		layout.draw(parentGc, x + 2, y);
		// Shell shell = new Shell(Display.getCurrent());
		// shell.setSize(w + 18, h + 100);
		// shell.setFont(parentGc.getFont());
		// shell.setBackground(parentGc.getBackground());
		// GridLayout layout = new GridLayout(1, false);
		// layout.marginHeight = 0;
		// layout.marginWidth = 0;
		// shell.setLayout(layout);
		// Image im = new Image(Display.getCurrent(), w, h);
		// GC gc = new GC(im);
		// Label br = new Label(shell, SWT.WRAP | SWT.INHERIT_DEFAULT);
		//
		// br.setText(text);
		// // Browser br = new Browser(shell, SWT.WRAP | SWT.INHERIT_DEFAULT);
		// br.setFont(shell.getFont());
		// GridData layoutData = new GridData(GridData.FILL_BOTH);
		// layoutData.widthHint = w - 2;
		// br.setLayoutData(layoutData);
		// // String wrapHtml = SymbolConverter.wrapHtml(html, shell);
		// // System.err.println(wrapHtml);
		// // br.setText(wrapHtml, true);
		// // shell.pack();
		// shell.layout();
		// while (true) {
		// if (!shell.getDisplay().readAndDispatch()) {
		// br.print(gc);
		// break;
		// }
		// }
		// shell.close();
		// parentGc.drawImage(im, x + 2, y);
		// im.dispose();
		// gc.dispose();
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
				// int r = (pixelValue & redMask) >>>
				// -fullImageData.palette.redShift;
				// int g = (pixelValue & greenMask) >>>
				// -fullImageData.palette.greenShift;
				// int b = (pixelValue & blueMask) >>>
				// -fullImageData.palette.blueShift;
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

	public static Image drawBorder(Image remoteImage, int border) {
		Rectangle bounds = remoteImage.getBounds();
		Display display = Display.getDefault();
		Image full = createTransparentImage(bounds.width + border * 2, bounds.height + border * 2);
		GC gc = new GC(full);
		// gc.setBackground(getBackground());
		// gc.fillRectangle(0, 0, bounds.width + border * 2, bounds.height +
		// border * 2);
		gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
		gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		gc.fillRoundRectangle(0, 0, bounds.width + border * 2, bounds.height + border * 2, border * 2, border * 2);
		gc.drawImage(remoteImage, border, border);
		gc.dispose();
		return full;
	}

	public static Image joinImages(Collection<Image> images, int max_width, int height) {
		if (images.size() == 0)
			return null;
		int width = getWidth(images);
		if (width <= 0)
			return null;
		else if (width > max_width)
			width = max_width;
		ImageData sourceData1 = images.iterator().next().getImageData();
		ImageData targetData = new ImageData(width, sourceData1.height, sourceData1.depth, sourceData1.palette);
		int x = 0;
		for (Image image : images) {
			ImageData id = image.getImageData();
			if (id.depth != sourceData1.depth)
				throw new IllegalArgumentException("Cannot merge images");
			if (x + id.width > width)
				break;
			overlay(id, targetData, x, 0);
			x += id.width;
		}
		return new Image(Display.getDefault(), targetData);
	}

	public static ImageData reEncodeIntoDirectPalette(ImageData imageData) {
		if (imageData.palette.redMask == 0xff0000 && imageData.palette.greenMask == 0xff00) {
			return imageData;
		}
		PaletteData paletteData = new PaletteData(0xff0000, 0xff00, 0xff);
		ImageData result = new ImageData(imageData.width, imageData.height, 32, paletteData);
		for (int x = 0; x < imageData.width; x++) {
			for (int y = 0; y < imageData.height; y++) {
				RGB rgb = imageData.palette.getRGB(imageData.getPixel(x, y));
				result.setPixel(x, y, paletteData.getPixel(rgb));
				result.setAlpha(x, y, imageData.getAlpha(x, y));
			}
		}
		return result;
	}

	public static Image getManaSymbolImage(String sym, String imagePath) {
		Image symImage = MagicUIActivator.getDefault().getImage(sym);
		if (symImage == null) {
			ImageDescriptor imageDescriptor = MagicUIActivator.getImageDescriptor(imagePath);
			if (imageDescriptor == null) {
				throw new MagicException("Cannot find images for " + imagePath + " " + sym);
			}
			ImageData imageData = reEncodeIntoDirectPalette(imageDescriptor.getImageData());
			setAlphaForManaCircles(imageData);
			//
			Image manaImage = new Image(Display.getDefault(), imageData);
			symImage = MagicUIActivator.getDefault().getImage(sym, manaImage);
		}
		return symImage;
	}

	public static Image createTransparentImage(int width, int height) {
		Display display = Display.getCurrent();
		Image tmpImage = new Image(display, width, height);
		ImageData id2 = tmpImage.getImageData();
		id2.alphaData = new byte[width * height];
		Image image = new Image(display, id2);
		tmpImage.dispose();
		return image;
	}

	public static void setAlphaForManaCircles(ImageData fullImageData) {
		int width = fullImageData.width;
		if (width > 16)
			return;
		int height = fullImageData.height;
		double cx = (width + 0.5) / 2, cy = (height) / 2;
		byte[] alphaData = new byte[height * width];
		int[] lineData = new int[width];
		for (int y = 0; y < height; y++) {
			fullImageData.getPixels(0, y, width, lineData, 0);
			byte[] alphaRow = new byte[width];
			for (int x = 0; x < width; x++) {
				int radius = 7;
				int al = FULL_OPAQUE;
				double x1 = Math.abs(x - cx);
				double y1 = Math.abs(y - cy);
				double dist = Math.sqrt(x1 * x1 + y1 * y1);
				if (dist >= radius)
					al = 0;
				else if (dist >= radius - 1)
					al = (int) (FULL_OPAQUE * (radius - dist));
				alphaRow[x] = (byte) al;
			}
			System.arraycopy(alphaRow, 0, alphaData, y * width, width);
		}
		fullImageData.alphaData = alphaData;
		fullImageData.transparentPixel = -1;
	}

	public static int getWidth(Collection<Image> images) {
		int width = 0;
		for (Image image : images) {
			width += image.getBounds().width;
		}
		return width;
	}

	public static void overlay(ImageData sourceData, ImageData targetData, int startX, int startY) {
		for (int x = 0; x < sourceData.width; x++) {
			for (int y = 0; y < sourceData.height; y++) {
				targetData.setPixel(startX + x, startY + y, sourceData.getPixel(x, y));
				targetData.setAlpha(startX + x, startY + y, sourceData.getAlpha(x, y));
			}
		}
	}
}
