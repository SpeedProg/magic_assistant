package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.MagicCardField;

public class ColorColumn extends AbstractColumn {
	public ColorColumn() {
		super(MagicCardField.COST);
	}

	@Override
	public String getText(Object element) {
		String text = super.getText(element);
		if (text.length() == 0)
			return text;
		return Colors.getColorName(text.toString());
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
