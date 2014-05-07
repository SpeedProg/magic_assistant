package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.MagicCardField;

public class TypeColumn extends GenColumn {
	public TypeColumn() {
		super(MagicCardField.TYPE, "Type");
	}

	@Override
	public int getColumnWidth() {
		return 200;
	}
}
