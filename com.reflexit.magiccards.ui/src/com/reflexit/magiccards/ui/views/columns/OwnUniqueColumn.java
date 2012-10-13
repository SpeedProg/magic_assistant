package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;

/**
 * @author Alena
 * 
 */
public class OwnUniqueColumn extends GenColumn {
	/**
	 */
	public OwnUniqueColumn() {
		super(MagicCardFieldPhysical.OWN_UNIQUE, "Unique");
	}

	@Override
	public String getText(Object element) {
		int ucount = 0;
		if (element instanceof IMagicCardPhysical) {
			ucount = ((IMagicCardPhysical) element).getOwnUnique();
		} else if (element instanceof CardGroup) {
			ucount = ((CardGroup) element).getOwnUnique();
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