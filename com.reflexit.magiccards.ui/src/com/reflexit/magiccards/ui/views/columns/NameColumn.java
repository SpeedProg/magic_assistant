package com.reflexit.magiccards.ui.views.columns;

public class NameColumn extends GenColumn {
	public NameColumn(int dataIndex) {
		super(dataIndex, "Name");
	}

	public int getColumnWidth() {
		return 200;
	}
}
