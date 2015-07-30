package com.reflexit.magiccards.ui.gallery;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.reflexit.magiccards.core.CannotDetermineSetAbbriviation;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.ui.utils.ImageCreator;

final class MagicCardImageLabelProvider extends LabelProvider {
	private StructuredViewer viewer;

	public MagicCardImageLabelProvider(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	HashMap<IMagicCard, Image> map = new HashMap<>();

	public void dispose() {
		for (Image im : map.values()) {
			im.dispose();
		}
		map.clear();
	}

	public String getText(Object element) {
		if (element instanceof ICardGroup)
			return ((ICardGroup) element).getName();
		if (element instanceof IMagicCard) {
			return ((IMagicCard) element).getName();
		}
		return "";
	}

	public Image getImage(Object element) {
		// System.err.println("getting image for " + element + " " +
		// element.getClass());
		if (element instanceof CardGroup && ((CardGroup) element).getFieldIndex() != MagicCardField.NAME)
			return null;
		if (map.containsKey(element))
			return map.get(element);
		// System.err.println("loaidng");
		new Job("loading card") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final IMagicCard card = (IMagicCard) element;
					if (card == null) {
						return Status.OK_STATUS;
					}
					String path = ImageCreator.getInstance().createCardPath(card, true, false);
					final Image image = ImageCreator.getInstance().createCardImage(path, false);
					if (image == null) {
						Image it = ImageCreator.getInstance().getCardNotFoundImageTemplate();
						Image itCopy = new Image(Display.getDefault(), it, SWT.IMAGE_COPY);
						map.put(card, itCopy);
					} else {
						map.put(card, image);
					}
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							viewer.refresh(element, true);
							// System.err.println("setting real image for "
							// + card);
							// item.setImage(image);
							// item.getParent().redraw();
						}
					});
				} catch (CannotDetermineSetAbbriviation e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		}.schedule();
		return ImageCreator.getInstance().getCardNotFoundImageTemplate();
	}
}