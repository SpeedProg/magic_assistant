package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

/**
 * @author Alena
 * 
 */
public class OwnUniqueColumn extends GenColumn {
	/**
	 */
	public OwnUniqueColumn() {
		super(MagicCardField.UNIQUE, "Unique");
	}

	@Override
	public String getText(Object element) {
		int ucount = 0;
		if (element instanceof MagicCard) {
			ucount = ((MagicCard) element).getOwnUnique();
		} else if (element instanceof CardGroup) {
			ucount = ((CardGroup) element).getOwnUSize();
		} else if (element instanceof MagicCardPhysical) {
			if (((MagicCardPhysical) element).isOwn())
				ucount = 1;
		} else {
			return "";
		}
		return String.valueOf(ucount);
	}

	@Override
	public int getColumnWidth() {
		return 45;
	}
}