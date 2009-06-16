package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class NameColumn extends GenColumn {
	LinkedHashMap<String, IMagicCard> queue = new LinkedHashMap<String, IMagicCard>();
	Job imageLoadingJob = new Job("Loading set images") {
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			while (true) {
				IMagicCard card = null;
				String key;
				synchronized (queue) {
					if (queue.size() > 0) {
						key = queue.keySet().iterator().next();
						card = queue.get(key);
						queue.remove(key);
					} else
						return Status.OK_STATUS;
				}
				try {
					URL url = CardCache.createSetImageURL(card, true);
					Image image = MagicUIActivator.getDefault().getImageRegistry().get(key);
					if (image == null) {
						image = createNewSetImage(url);
						MagicUIActivator.getDefault().getImage(key, image);
					}
				} catch (Exception e) {
					// no image, skip
				}
			}
		}
	};

	public NameColumn() {
		super(MagicCardField.NAME, "Name");
		imageLoadingJob.setSystem(true);
	}

	@Override
	public int getColumnWidth() {
		return 200;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			URL url;
			try {
				url = CardCache.createSetImageURL(card, false);
				String key = url.toExternalForm();
				Image image = MagicUIActivator.getDefault().getImageRegistry().get(key);
				File file = new File(url.getFile());
				if (image == null) {
					if (file.exists()) {
						image = createNewSetImage(url);
						return MagicUIActivator.getDefault().getImage(key, image);
					} else {
						synchronized (queue) {
							queue.put(key, card);
						}
						imageLoadingJob.schedule(0);
						return null;
					}
				} else {
					return image;
				}
			} catch (IOException e) {
				// huh
			}
		}
		return super.getImage(element);
	}

	private Image createNewSetImage(URL url) {
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
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.columns.ColumnManager#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof CardGroup) {
			return ((CardGroup) element).getName();
		}
		return super.getText(element);
	}
}
