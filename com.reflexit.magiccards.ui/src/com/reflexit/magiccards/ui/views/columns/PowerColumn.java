package com.reflexit.magiccards.ui.views.columns;

public class PowerColumn extends GenColumn {
	private String fullname;

	public PowerColumn(int i, String columnName, String fullname) {
		super(i, columnName);
		this.fullname = fullname;
	}

	@Override
    public String getColumnFullName() {
		return this.fullname;
	}

	@Override
    public int getColumnWidth() {
		return 30;
	}
}
