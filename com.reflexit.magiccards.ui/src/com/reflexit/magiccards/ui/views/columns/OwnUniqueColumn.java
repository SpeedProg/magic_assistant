package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCardField;

/**
 * @author Alena
 *
 */
public class OwnUniqueColumn extends GenColumn {
	/**
	 */
	public OwnUniqueColumn() {
		super(MagicCardField.OWN_UNIQUE, "Own Unique");
	}

	@Override
	public String getText(Object element) {
		int ucount = 0;
		if (element instanceof IMagicCardPhysical) {
			ucount = ((IMagicCardPhysical) element).getOwnUnique();
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