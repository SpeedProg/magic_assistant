package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.MagicCardField;

/**
 * @author Alena
 * 
 */
public class OwnTotalCountColumn extends CountColumn {
	public OwnTotalCountColumn() {
		super(MagicCardField.OWN_TOTAL, "Own Total");
	}

	@Override
	public int getColumnWidth() {
		return 60;
	}

	@Override
	public String getToolTipText(Object element) {
		return "How many instances of this card you own, regardless of set";
	}

	@Override
	protected boolean canEditElement(Object element) {
		return false;
	}
}