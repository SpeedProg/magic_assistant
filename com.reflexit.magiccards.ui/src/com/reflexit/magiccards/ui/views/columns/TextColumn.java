package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.MagicCardField;

public class TextColumn extends GenColumn {
	public TextColumn() {
		super(MagicCardField.TEXT, "Text");
	}

	@Override
	public String getToolTipText(Object element) {
		String text = getText(element);
		text = text.replaceAll("<br>", "\n");
		return text;
	}
}
