package com.reflexit.magiccards.ui.views.columns;


public class GenColumn extends ColumnManager {
	private String colName;

	public GenColumn(int i, String columnName) {
		super(i);
		this.colName = columnName;
	}

	@Override
	public String getColumnName() {
		return this.colName;
	}
}
