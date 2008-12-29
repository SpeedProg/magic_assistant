package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.Color;

import com.reflexit.magiccards.core.model.MagicCardField;

public class OracleTextColumn extends GenColumn {
	public OracleTextColumn() {
		super(MagicCardField.ORACLE, "Oracle Text");
	}

	@Override
	public String getToolTipText(Object element) {
		String text = getText(element);
		text = text.replaceAll("<br>", "\n");
		return text;
	}

	@Override
	public Color getToolTipBackgroundColor(Object object) {
		return super.getToolTipBackgroundColor(object);
	}
}
