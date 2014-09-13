package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.MagicCardField;

public class IdColumn extends GenColumn {
	public IdColumn() {
		super(MagicCardField.ID, "Card Id");
	}

	@Override
	public int getColumnWidth() {
		return 60;
	}
}
