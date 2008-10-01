package com.reflexit.magiccards.ui.views.columns;


public class IdColumn extends ColumnManager {
	public IdColumn(int column) {
		super(column);
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
