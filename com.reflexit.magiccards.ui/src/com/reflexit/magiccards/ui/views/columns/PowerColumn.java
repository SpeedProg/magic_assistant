package com.reflexit.magiccards.ui.views.columns;

public class PowerColumn extends GenColumn {
	private String fullname;

	public PowerColumn(int i, String columnName, String fullname) {
		super(i, columnName);
		this.fullname = fullname;
	}

	public String getColumnFullName() {
		return this.fullname;
	}

	public int getColumnWidth() {
		return 30;
	}
}
