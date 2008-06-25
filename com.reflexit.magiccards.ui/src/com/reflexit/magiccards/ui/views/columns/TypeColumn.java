package com.reflexit.magiccards.ui.views.columns;

public class TypeColumn extends GenColumn {
	public TypeColumn(int dataIndex) {
		super(dataIndex, "Type");
	}

	public int getColumnWidth() {
		return 200;
	}
}
