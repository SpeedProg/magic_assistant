package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.MagicCardField;

public class IdColumn extends AbstractColumn {
	public IdColumn() {
		super(MagicCardField.ID);
	}

	@Override
	public int getColumnWidth() {
		return 60;
	}

	@Override
	public String getColumnName() {
		return "Card Id";
	}
}
