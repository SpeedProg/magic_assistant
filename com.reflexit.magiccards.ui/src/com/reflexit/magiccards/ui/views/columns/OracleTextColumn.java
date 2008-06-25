package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.Color;

public class OracleTextColumn extends GenColumn {
	public OracleTextColumn(int dataIndex) {
		super(dataIndex, "Oracle Text");
	}

	public String getToolTipText(Object element) {
		String text = getText(element);
		text = text.replaceAll("<br>", "\n");
		return text;
	}

	public Color getToolTipBackgroundColor(Object object) {
		return super.getToolTipBackgroundColor(object);
	}
}
