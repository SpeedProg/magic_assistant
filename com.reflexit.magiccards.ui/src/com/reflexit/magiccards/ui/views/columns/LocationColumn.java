package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.MagicCardPhisical;

/**
 * @author Alena
 *
 */
public class LocationColumn extends GenColumn {
	/**
	 * @param i
	 * @param columnName
	 */
	public LocationColumn(int i, String columnName) {
		super(i, columnName);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof MagicCardPhisical) {
			MagicCardPhisical m = (MagicCardPhisical) element;
			String loc = m.getLocation();
			if (loc == null)
				return "";
			if (loc.endsWith(".xml")) {
				return loc.replaceFirst("\\.xml$", "");
			}
			return loc;
		} else {
			return "";
		}
	}
}