package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.Image;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.utils.ImageCreator;

public class SetColumn extends GenColumn {
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
	public int getColumnWidth() {
		return 150;
	}

	@Override
	public Image getImage(Object element) {
		if (isShowImage()) {
			if (element instanceof IMagicCard) {
				IMagicCard card = (IMagicCard) element;
				return ImageCreator.getInstance().getSetImage(card);
			}
		}
		return super.getImage(element);
	}
}
