package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.IMagicCard;

public class ColorColumn extends ColumnManager {
	public ColorColumn(int column) {
		super(column);
	}

	@Override
	public String getText(Object element) {
		String cost = ((IMagicCard) element).getByIndex(this.dataIndex);
		return Colors.getColorName(cost);
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
