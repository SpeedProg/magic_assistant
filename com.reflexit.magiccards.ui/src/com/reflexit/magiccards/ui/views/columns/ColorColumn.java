package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class ColorColumn extends AbstractColumn {
	public ColorColumn() {
		super(MagicCardField.COST);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			Object cost = ((IMagicCard) element).getCost();
			if (cost == null)
				return "";
			return Colors.getColorName(cost.toString());
		}
		return super.getText(element);
	}

	@Override
	public String getColumnName() {
		return "Color";
	}

	@Override
	public int getColumnWidth() {
		return 40;
	}
}
