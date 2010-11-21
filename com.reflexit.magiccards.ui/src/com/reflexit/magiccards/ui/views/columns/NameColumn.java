package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.Image;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.ui.utils.ImageCreator;

public class NameColumn extends GenColumn {
	public NameColumn() {
		super(MagicCardField.NAME, "Name");
	}

	@Override
	public int getColumnWidth() {
		return 200;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			return ImageCreator.getInstance().getSetImage(card);
		}
		return super.getImage(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.reflexit.magiccards.ui.views.columns.ColumnManager#getText(java.lang
	 * .Object)
	 */
	@Override
	public String getText(Object element) {
		// if (element instanceof CardGroup) {
		// return ((CardGroup) element).getName();
		// }
		return super.getText(element);
	}
}
