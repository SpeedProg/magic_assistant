package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.MagicCardField;

final class CollectorsNumberColumn extends GenColumn {
	public CollectorsNumberColumn() {
		super(MagicCardField.COLLNUM, "Num");
	}

	@Override
	public String getColumnFullName() {
		return "Collector's Number";
	}

	@Override
	public int getColumnWidth() {
		return 40;
	}
}